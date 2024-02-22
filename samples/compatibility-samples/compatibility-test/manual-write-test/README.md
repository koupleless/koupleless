<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experimental Content

Test whether the business code can be compatible with the multi-classLoader mode of sofaArk without
modifying the Maven project structure.

# Involved Class Explanation:

- mock.BaseBootStrap: The base boot class, simulating the base startup.
- mock.ClassBeIncludedInBizClassLoader: Simulates the class loaded in BizClassLoader.
- mock.CommonPackageInBase: Simulates the class loaded by the base.
- mock.MockTestRunner: Simulates the test framework, responsible for executing specific test cases.
- mock.TestClassInBizClassLoaderA: Specific test class of Biz.
- UseTestBizClassLoaderTest: Constructs a test-use TestBiz, running the test case of
  mock.TestClassInBizClassLoaderA.

Among them, mock.TestClassInBizClassLoaderA contains specific test logic. These logics are
responsible for verifying whether classes have been correctly loaded, as follows:

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

Implementation Steps
Execute the test case of UseTestBizClassLoaderTest, verify whether the test case passes.
![manual-test-success.png][./manual-test-success.png]
The test passes, indicating that each of the simulated classes can be correctly loaded. Thus,
without splitting the module, within the original code repository, a simple TestBiz has completed
the compatibility mode verification work of SOFAArk.
If the verification passes, subsequent steps can follow the class loading configuration of TestBiz
to divide modules.