<div align="center">

[English](./README.md) | 简体中文

</div>

# 实验内容

在不修改 maven 项目结构的情况下，测试业务代码是否能兼容 sofaArk 的多 classLoader 模式。

# 涉及类说明:

- mock.BaseBootStrap： 基座启动类, 模拟基座启动。
- mock.ClassBeIncludedInBizClassLoader： 模拟 BizClassLoader 中加载的类。
- mock.CommonPackageInBase: 模拟基座加载的类。
- mock.MockTestRunner: 模拟单测框架，负责执行具体的测试用例。
- mock.TestClassInBizClassLoaderA: Biz 的具体测试类。
- UseTestBizClassLoaderTest: 构造一个测试用的 TestBiz，运行 mock.TestClassInBizClassLoaderA 的测试用例。

其中，mock.TestClassInBizClassLoaderA 包含了具体的测试逻辑，这些逻辑负责校验类是否有被正确的加载，内容如下：

```java
   public void test() {
    System.out.println("test method called");
    Preconditions.checkState(
            BaseBootStrap.IS_BOOTSTRAP_BASE_CALLED.get(),
            "bootstrapBase should be called"
    );

    Preconditions.checkState(
            !(CommonPackageInBase.class.getClassLoader() instanceof BizClassLoader),
            "CommonPackageInBase should not be loaded by BizClassLoader"
    );

    Preconditions.checkState(
            Thread.currentThread().getContextClassLoader() instanceof BizClassLoader,
            "TCCL should be BizClassLoader"
    );

    Preconditions.checkState(
            this.getClass().getClassLoader() instanceof BizClassLoader,
            "Class ClassLoader should be BizClassLoader"
    );

    Preconditions.checkState(
            ClassBeIncludedInBizClassLoader.class.getClassLoader() instanceof BizClassLoader,
            "Class ClassBeIncludedInBizClassLoader should be BizClassLoader"
    );
}
```

# 实现步骤

执行 UseTestBizClassLoaderTest 的测试用例，校验测试用例是否通过。
![manual-test-success.png][./manual-test-success.png]
测试通过，说明各个模拟的类能够正确地被加载，从而，在不拆分模块的前提下，在原有的代码仓库内，通过一个简单的
TestBiz 完成了对 SOFAArk 兼容模式的验证工作。
如果验证通过了，后续可以按照 TestBiz 的类加载配置，去划分模块。
