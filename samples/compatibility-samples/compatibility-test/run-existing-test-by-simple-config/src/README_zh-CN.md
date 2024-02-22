<div align="center">

[English](./README.md) | 简体中文

</div>

# 实验内容
只靠配置文件，在不写代码的情况下，测试业务代码是否能兼容 sofaArk 的多 classLoader 模式。

# 工程说明
## 要点一、添加兼容性集成测试插件
在 pom.xml 中添加兼容性集成测试插件，配置如下：
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.alipay.sofa.koupleless</groupId>
            <artifactId>koupleless-test-suite</artifactId>
            <executions>
                <execution>
                    <id>compatible-test</id>
                    <goals>
                        <goal>compatible-test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
## 要点二、添加兼容性测试配置文件
在 src/test/resources 目录下添加兼容性测试配置文件，文件名为 sofa-ark-compatible-test-config.yaml ，配置如下：
```yaml
testFramework: junit4  #测试框架
testBizConfigs: #模拟不同的 biz 使用方式
  - name: 'sofa-ark-compatible-test' #当前模拟的用例名称
    bootstrapClass: 'pkg.BootstrapClass' #模拟基座启动的类
    testClasses: #需要在 bizClassLoader 中加载的测试类
      - 'pkg.OriginalTest'
    loadByBizClassLoaderPatterns: #需要在 bizClassLoader 中加载的类，可以通过 java 正则表达式匹配,其余类默认在 baseClassLoader 中加载
      - 'pkg.CommonClass'
```
## 要点三、执行兼容性测试
在项目下执行如下命令，执行兼容性测试：
```shell
mvn com.alipay.sofa.koupleless:koupleless-test-suite:compatible-test
```

## 要点四、测试用例逻辑说明：
- pkg.BootstrapClass.java: 模拟在基座类启动时初始化有关逻辑。
- pkg.CommonClass.java: 模拟基座加载的公共类。
- pkg.CommonClassAlwaysInBase.java: 模拟委托给基座的类。
- pkg.OriginalTest.java: 模拟在 bizClassLoader 中加载的测试类, 这个测试类的逻辑会打印各种 ClassLoader，方便开发者观察他们是否符合预期。

# 实验步骤 
## 步骤一：执行原始测试，观察测试内容。
执行 pkg.OriginalTest 测试类，观察测试终端输出，输出如下：
```text
Hello CommonClass from Class ClassLoader: sun.misc.Launcher$AppClassLoader
Hello CommonClassAlwaysInBase from Class ClassLoader: sun.misc.Launcher$AppClassLoader
Hello OriginalTest from TCCL: sun.misc.Launcher$AppClassLoader
Hello OriginalTest from Class ClassLoader: sun.misc.Launcher$AppClassLoader
```
都由基座加载。

## 步骤二：执行兼容性测试，观察测试内容。
执行命令 mvn com.alipay.sofa.koupleless:koupleless-test-suite:compatible-test ，观察测试终端输出，输出如下：
```text
[INFO] sofa-ark-compatible-test:0.0.1-SNAPSHOT, CompatibleTestStarted
BootstrapClass
Hello CommonClass from Class ClassLoader: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
Hello CommonClassAlwaysInBase from Class ClassLoader: java.net.URLClassLoader
Hello OriginalTest from TCCL: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
Hello OriginalTest from Class ClassLoader: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
[INFO] sofa-ark-compatible-test:0.0.1-SNAPSHOT, CompatibleTestFinished
```
可以看到:
1. BootstrapClass 被调用了。
2. CommonClassAlwaysInBase 由基座加。
3. CommonClass 和 OriginalTest 由 BizClassLoader 加载。

符合我们的预期。

# 总结
通过简单的 mvn 指令，用户可以在不修改工程的前提下进行快速的验证，验证结果符合预期后，可以按照模拟的类加载配置，去划分模块。