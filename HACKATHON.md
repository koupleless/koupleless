# 黑客松高手学习指南

本指南面向已了解 Koupleless 基础知识，希望在黑客松活动中快速上手高阶场景的参赛者。完成本指南后，您将具备以下能力：

- 掌握存量应用低成本改造为模块化架构的方法
- 理解类加载隔离与委托机制，具备排查类冲突问题的能力
- 熟练使用模块瘦身技术，让模块启动时间 < 5 秒
- 实现基座与模块、模块与模块之间的 Spring Bean 调用
- 完成模块动态部署与热更新的完整链路

---

## 前提条件

在开始高阶学习前，请确认已完成以下基础内容：

1. [快速开始：模块开发](https://koupleless.io/docs/quick-start/module_dev/)
2. [快速开始：模块开发与运维](https://koupleless.io/docs/quick-start/module_dev_and_ops/)
3. 本地环境已安装：JDK 8/11/17（均支持，建议 JDK 11+）、Maven 3.8+、[arkctl](https://github.com/koupleless/arkctl/releases/)

---

## 高阶挑战一：存量应用模块化改造

**目标**：将一个已有的标准 SpringBoot 应用改造为 Koupleless 模块，并合并部署到基座中，**不修改任何业务代码**。

### 改造步骤

#### 1. 基座改造

在基座的 `pom.xml` 中添加 Koupleless 运行时依赖与 Web 多模块插件：

```xml
<properties>
    <koupleless.runtime.version>2.1.11</koupleless.runtime.version>
    <sofa.ark.version>3.1.11</sofa.ark.version>
</properties>
<dependencies>
    <dependency>
        <groupId>com.alipay.sofa.koupleless</groupId>
        <artifactId>koupleless-base-starter</artifactId>
        <version>${koupleless.runtime.version}</version>
    </dependency>
    <!-- 多 Web 模块同端口部署 -->
    <dependency>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>web-ark-plugin</artifactId>
    </dependency>
</dependencies>
```

在 `application.properties` 中为基座设置应用名：

```properties
spring.application.name=base-app
```

#### 2. 模块改造（以存量应用为例）

在模块的 `pom.xml` 中添加打包插件，**无需修改任何业务代码**：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.alipay.sofa</groupId>
            <artifactId>sofa-ark-maven-plugin</artifactId>
            <version>${sofa.ark.version}</version>
            <executions>
                <execution>
                    <id>default-cli</id>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <skipArkExecutable>true</skipArkExecutable>
                <outputDirectory>./target</outputDirectory>
                <bizName>my-module</bizName>
                <webContextPath>/my-module</webContextPath>
                <declaredMode>true</declaredMode>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 3. 模块瘦身（关键步骤）

在模块项目根目录创建 `conf/ark/bootstrap.properties`，排除与基座共享的依赖：

```properties
# 排除 Spring 全家桶、日志框架（基座已包含），多个 groupId 用逗号分隔
excludeGroupIds=org.springframework,org.springframework.boot,org.springframework.cloud,ch.qos.logback,org.slf4j,log4j
# 排除通用工具库（指定 groupId:artifactId），多个用逗号分隔
excludes=org.apache.commons:commons-lang3,commons-beanutils:commons-beanutils
```

#### 4. 构建与验证

```bash
# 构建模块（在模块目录下执行）
mvn clean package -DskipTests

# 使用 arkctl 部署到本地运行的基座
arkctl deploy ./target/my-module-1.0.0-ark-biz.jar

# 验证模块是否已部署成功
curl http://localhost:8080/my-module/
```

**进阶挑战**：尝试将两个以上的独立应用同时部署到同一个基座，观察端口复用和资源节省效果。

---

## 高阶挑战二：跨模块服务调用

**目标**：实现基座调用模块服务、模块调用基座服务、模块间互相调用三种场景。

### 场景架构

```
┌─────────────────────────────────┐
│              基座                │
│  ┌─────────┐    ┌─────────────┐ │
│  │  服务定义 │    │  服务消费者  │ │
│  └─────────┘    └─────────────┘ │
│         │              ↑        │
│    ┌────┼──────────────┼────┐   │
│    │    ↓    模块 A         │   │
│    │ 服务实现 → 调用模块B   │   │
│    └───────────────────────┘   │
│    ┌───────────────────────┐   │
│    │       模块 B           │   │
│    │    服务实现             │   │
│    └───────────────────────┘   │
└─────────────────────────────────┘
```

### 实现方式

#### 基座调用模块：`SpringServiceFinder`

```java
// 基座 Controller 调用模块中的服务
@RestController
public class BaseController {

    @RequestMapping(value = "/order", method = RequestMethod.GET)
    public String handleOrder() {
        // 获取模块 A 中名为 orderService 的 Bean
        OrderService orderService = SpringServiceFinder.getModuleService(
            "module-a",           // bizName，对应打包插件中的 bizName
            "0.0.1-SNAPSHOT",     // bizVersion
            "orderService",       // Spring Bean 名称
            OrderService.class
        );
        return orderService.process();
    }
}
```

#### 模块调用基座：`@AutowiredFromBase`

```java
// 模块中注入基座的 Bean
@RestController
public class ModuleController {

    @AutowiredFromBase
    private DataSource dataSource;          // 复用基座数据源

    @AutowiredFromBase(name = "rpcClient")
    private RpcClient rpcClient;            // 复用基座 RPC 客户端

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        // 直接使用基座的数据源，无需在模块中配置
        return "using base datasource: " + dataSource.getClass().getSimpleName();
    }
}
```

#### 模块间调用：`@AutowiredFromBiz`

```java
// 模块 A 中调用模块 B 的服务
@Service
public class ModuleAService {

    @AutowiredFromBiz(bizName = "module-b", bizVersion = "0.0.1-SNAPSHOT")
    private PaymentService paymentService;

    public String doCheckout() {
        return paymentService.pay(100.0);
    }
}
```

### 通信类定义最佳实践

将接口类和通信数据模型单独抽取为独立 jar（`shared-api` 模块），基座和所有业务模块共同依赖它：

```
my-project/
├── shared-api/          # 接口定义（基座和模块共同依赖）
│   └── src/main/java/
│       ├── OrderService.java      # 接口
│       └── OrderDTO.java          # 数据传输对象
├── base/                # 基座
└── module-a/            # 模块
```

---

## 高阶挑战三：模块动态热更新

**目标**：在不重启基座（不中断在线业务）的情况下，完成模块代码的热更新与回滚。

### 动态部署流程

```bash
# 1. 修改模块代码后重新打包
# 方式一：在模块目录下单独打包
cd module-a && mvn clean package -DskipTests
# 方式二：在多模块父项目根目录下，用 -pl 指定子模块
# mvn clean package -DskipTests -pl module-a -am

# 2. 使用 arkctl 热部署新版本（会自动卸载旧版本并安装新版本）
arkctl deploy ./module-a/target/module-a-1.0.1-ark-biz.jar

# 3. 验证新版本已生效（无需重启基座）
curl http://localhost:8080/module-a/version
# 输出：v1.0.1

# 4. 如需回滚，重新部署旧版本 jar 包即可
arkctl deploy ./module-a/target/module-a-1.0.0-ark-biz.jar
```

### 热更新注意事项

1. **模块版本管理**：每次热更新建议更新模块 `bizVersion`，便于区分和回滚
2. **状态管理**：模块卸载时，内存中的状态不会自动迁移，需要通过持久化存储（数据库、缓存）保证状态一致性
3. **连接池复用**：数据源、线程池等资源建议定义在基座，模块通过 `@AutowiredFromBase` 复用，避免热更新时资源泄漏
4. **模块瘦身控制**：单模块包体积建议控制在 5MB 以下，以支持频繁热部署

---

## 高阶挑战四：类加载隔离问题排查

**目标**：理解类加载委托机制，能独立排查和解决类冲突问题。

### 类加载优先级

```
模块 ClassLoader
    ↓ (找不到时委托)
基座 ClassLoader
    ↓ (找不到时委托)
Bootstrap ClassLoader (JDK)
```

### 常见问题与解决方案

#### 问题 1：`ClassCastException` 或 `ClassNotFoundException`

**现象**：模块调用基座服务时出现 `ClassCastException`。

**原因**：接口类被模块和基座各自加载了一次，形成两个不同的 Class 对象，导致类型转换失败。

**解决方案**：将接口类下沉到基座，模块通过 `provided` 方式依赖：

```xml
<!-- 模块 pom.xml 中 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>shared-api</artifactId>
    <scope>provided</scope>  <!-- 关键：声明为 provided，不打入模块包 -->
</dependency>
```

#### 问题 2：SPI 机制失效

**现象**：`ServiceLoader` 或日志框架等 SPI 机制在模块中不生效。

**解决方案**：在模块的 `conf/ark/bootstrap.properties` 中将相关依赖加入白名单（`includes`），让模块自带一份实现：

```properties
# 模块自带 logback 实现，避免使用基座的日志配置
includes=ch.qos.logback:logback-classic,ch.qos.logback:logback-core
```

#### 问题 3：`declaredMode` 依赖缺失

**现象**：模块启动失败，提示某个类找不到。

**原因**：开启 `declaredMode` 后，模块中 `provided` 的依赖需要在基座中存在。

**排查方法**：
```bash
# 检查基座已加载的 jar 包
curl http://localhost:1238/queryAllBiz
```

---

## 高阶挑战五：中台模式实战

**目标**：构建一个基座定义 SPI、多个模块实现不同策略的中台架构。

### 架构设计

```
基座（中台核心）：
- 定义统一的服务接口 SPI
- 实现路由逻辑，根据请求参数选择不同模块的实现

模块 A（策略一）：
- 实现基座的 SPI 接口
- 提供特定业务场景的处理逻辑

模块 B（策略二）：
- 实现相同的 SPI 接口
- 提供另一种业务场景的处理逻辑
```

### 代码示例

**基座定义 SPI（`shared-api` 模块中）**：

```java
// 策略接口
public interface RecommendStrategy {
    List<Item> recommend(User user, List<Item> items);
    String getStrategyName();
}
```

**基座路由逻辑**：

```java
@RestController
public class RecommendController {

    @RequestMapping("/recommend/{strategy}")
    public List<Item> recommend(@PathVariable String strategy,
                                 @RequestBody RecommendRequest request) {
        // 根据策略名（即模块的 bizName）找到对应模块的所有 RecommendStrategy 实现
        // 第一个参数为 bizName，第二个参数为 bizVersion（null 表示不限版本），第三个参数为接口类型
        Map<String, RecommendStrategy> strategies =
            SpringServiceFinder.listModuleServices(strategy, null, RecommendStrategy.class);

        RecommendStrategy impl = strategies.values().stream().findFirst()
            .orElseThrow(() -> new RuntimeException("Strategy not found: " + strategy));

        return impl.recommend(request.getUser(), request.getItems());
    }
}
```

**模块 A 实现（热销排序策略）**：

```java
@Service
public class HotSaleStrategy implements RecommendStrategy {

    @Override
    public List<Item> recommend(User user, List<Item> items) {
        // 按销量降序排序
        return items.stream()
            .sorted(Comparator.comparingInt(Item::getSaleCount).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "hot-sale";
    }
}
```

**模块 B 实现（个性化推荐策略）**：

```java
@Service
public class PersonalizedStrategy implements RecommendStrategy {

    @Override
    public List<Item> recommend(User user, List<Item> items) {
        // 根据用户画像个性化排序
        return items.stream()
            .sorted(Comparator.comparingDouble(item -> -matchScore(user, item)))
            .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "personalized";
    }
}
```

---

## 工具链速查

| 操作 | 命令 |
|------|------|
| 部署模块 | `arkctl deploy <path-to-ark-biz.jar>` |
| 查询已安装模块 | `arkctl status` |
| 卸载模块 | `arkctl undeploy <bizName>:<bizVersion>` |
| 查看模块日志 | 基座日志中搜索 `[bizName]` 前缀 |
| 本地联调调试 | IDEA 中直接启动基座，`arkctl deploy` 部署模块 |

HTTP 接口（arklet 默认端口 1238）：

```bash
# 查询所有已安装模块
curl http://localhost:1238/queryAllBiz

# 安装模块
curl -X POST http://localhost:1238/installBiz \
  -H "Content-Type: application/json" \
  -d '{"bizName":"my-module","bizVersion":"0.0.1","bizUrl":"file:///path/to/module.jar"}'

# 卸载模块
curl -X POST http://localhost:1238/uninstallBiz \
  -H "Content-Type: application/json" \
  -d '{"bizName":"my-module","bizVersion":"0.0.1"}'
```

---

## 黑客松评分维度参考

| 维度 | 描述 | 权重 |
|------|------|------|
| **模块化改造完整性** | 存量应用无代码改动完成模块化 | 25% |
| **资源节省效果** | 多模块合并后内存与启动时间对比 | 25% |
| **热更新可靠性** | 零停机情况下完成模块迭代 | 20% |
| **跨模块协作** | SPI 模式或模块间服务调用实现 | 20% |
| **代码质量** | 模块瘦身、编码规范、错误处理 | 10% |

---

## 参考资源

- [官方文档](https://koupleless.io/docs/)
- [快速开始](https://koupleless.io/docs/quick-start/)
- [模块开发教程](https://koupleless.io/docs/tutorials/module-development/)
- [模块运维教程](https://koupleless.io/docs/tutorials/module-operation/)
- [样例工程](https://github.com/koupleless/samples)
- [视频教程与交互式实验](https://koupleless.io/docs/video-training/)
- [FAQ 常见问题](https://koupleless.io/docs/faq/)
- [DingTalk 答疑群：24970018417](https://koupleless.io)
