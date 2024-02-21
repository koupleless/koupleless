/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkServiceContainerSingleton;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import mock.ClassBeIncludedInBizClassLoader;
import mock.MockTestRunner;
import mock.TestClassInBizClassLoaderA;
import mock.BaseBootStrap;
import org.junit.jupiter.api.Test;

import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @author CodeNoobKing
 * @date 2024/2/21
 */
public class UseTestBizClassLoaderTest {

    static {
        SOFAArkServiceContainerSingleton.init(UseTestBizClassLoaderTest.class.getClassLoader());
    }

    private SOFAArkTestBiz createTestBiz() {
        // 基座 ClassLoader
        URLClassLoader baseClassLoader = (URLClassLoader) Thread.currentThread()
            .getContextClassLoader();

        // 测试类
        ArrayList<String> testClasses = new ArrayList<>();
        // TestClassInBizClassLoaderA 才是我们真正想要测试的类。
        testClasses.add(TestClassInBizClassLoaderA.class.getName());

        // 包含在测试 BIZ ClassLoader 中的类
        ArrayList<String> includedInBizClassLoaderPatterns = new ArrayList<>();
        includedInBizClassLoaderPatterns.add(".*"
                                             + ClassBeIncludedInBizClassLoader.class
                                                 .getSimpleName() + ".*");

        // 创建测试 BIZ
        return new SOFAArkTestBiz(
            // bootstrap class 用来模拟一些基座初始化的行为。
            BaseBootStrap.class.getName(), "test", "1.0.0", testClasses,
            includedInBizClassLoaderPatterns, baseClassLoader);
    }

    @Test
    public void testUseTestBizClassLoader() throws Exception {
        // 首先，我们需要创建模拟用的测试 BIZ。
        SOFAArkTestBiz testBiz = createTestBiz();
        // 其次，我们需要执行测试, 内容。
        testBiz.executeTest(() -> {
            // 我们可能需要一个公共的 test runner 来执行测试，比如 TestNG, JUnit5, JUnit4 等的 Runner。
            MockTestRunner testRunner = new MockTestRunner();

            // 获得由 BIZ ClassLoader 加载的测试类。
            for (Class<?> testClass : testBiz.getTestClasses()) {
                testRunner.execute(testClass);
            }
        }).get();
    }
}
