<div align="center">

[English](./README.md) | 简体中文

</div>

# Experimental Content

Test whether business code can be compatible with sofaArk's multi-classLoader mode without writing
any code, relying only on configuration files.

# Project Explanation

## Key Point One: Add Compatibility Integration Testing Plugin

Add the compatibility integration testing plugin to the `pom.xml` with the following configuration:

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

## Key Point Two: Add Compatibility Testing Configuration File

Add a compatibility testing configuration file named `sofa-ark-compatible-test-config.yaml` in
the `src/test/resources` directory, with the following configuration:

```yaml
testFramework: junit4  # Test framework
testBizConfigs: # Simulate different biz usage methods
  - name: 'sofa-ark-compatible-test' # The current simulated case name
    bootstrapClass: 'pkg.BootstrapClass' # The class that simulates the base start-up
    testClasses: # Classes that need to be loaded in the bizClassLoader
      - 'pkg.OriginalTest'
    loadByBizClassLoaderPatterns: # Classes that need to be loaded by the bizClassLoader, can be matched using Java regular expressions, other classes are loaded by the baseClassLoader by default
      - 'pkg.CommonClass'
```

## Key Point Three: Execute Compatibility Testing

Execute the following command under the project to perform compatibility testing:

```shell
mvn com.alipay.sofa.koupleless:koupleless-test-suite:compatible-test
```

## Key Point Four: Test Case Logic Explanation:

- pkg.BootstrapClass.java: Simulates the initialization of related logic when the base class starts.
- pkg.CommonClass.java: Simulates a common class loaded by the base.
- pkg.CommonClassAlwaysInBase.java: Simulates a class delegated to the base.
- pkg.OriginalTest.java: Simulates a test class loaded in the bizClassLoader, this test class's
  logic will print various ClassLoaders, helping developers observe if they meet expectations.

# Experimental Steps

## Step One: Execute the Original Test, Observe Test Content.

Execute the `pkg.OriginalTest` test class, observe the terminal output, which is as follows:

```text
Hello CommonClass from Class ClassLoader: sun.misc.Launcher$AppClassLoader
Hello CommonClassAlwaysInBase from Class ClassLoader: sun.misc.Launcher$AppClassLoader
Hello OriginalTest from TCCL: sun.misc.Launcher$AppClassLoader
Hello OriginalTest from Class ClassLoader: sun.misc.Launcher$AppClassLoader
```

All loaded by the base.

## Step Two: Execute Compatibility Testing, Observe Test Content.

Execute the command `mvn com.alipay.sofa.koupleless:koupleless-test-suite:compatible-test`, observe
the terminal output, which is as follows:

```text
[INFO] sofa-ark-compatible-test:0.0.1-SNAPSHOT, CompatibleTestStarted
BootstrapClass
Hello CommonClass from Class ClassLoader: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
Hello CommonClassAlwaysInBase from Class ClassLoader: java.net.URLClassLoader
Hello OriginalTest from TCCL: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
Hello OriginalTest from Class ClassLoader: com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader
[INFO] sofa-ark-compatible-test:0.0.1-SNAPSHOT, CompatibleTestFinished
```

It can be seen that:

1. BootstrapClass was called.
2. CommonClassAlwaysInBase was loaded by the base.
3. CommonClass and OriginalTest were loaded by the BizClassLoader.

This meets our expectations.

# Summary

With simple Maven commands, users can quickly verify without modifying the project, and after the
verification results meet expectations, they can divide modules according to the simulated class
loading configuration.