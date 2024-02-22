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
package mock;

import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.spi.model.Biz;
import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author CodeNoobKing
 * @date 2024/2/21
 */
public class TestClassInBizClassLoaderA {

    // 这个方法会用来教研，类加载的行为是否符合预期。
    public void test() {
        System.out.println("test method called");
        Preconditions.checkState(BaseBootStrap.IS_BOOTSTRAP_BASE_CALLED.get(),
            "bootstrapBase should be called");

        Preconditions.checkState(
            !(CommonPackageInBase.class.getClassLoader() instanceof BizClassLoader),
            "CommonPackageInBase should not be loaded by BizClassLoader");

        Preconditions.checkState(
            Thread.currentThread().getContextClassLoader() instanceof BizClassLoader,
            "TCCL should be BizClassLoader");

        Preconditions.checkState(this.getClass().getClassLoader() instanceof BizClassLoader,
            "Class ClassLoader should be BizClassLoader");

        Preconditions.checkState(
            ClassBeIncludedInBizClassLoader.class.getClassLoader() instanceof BizClassLoader,
            "Class ClassBeIncludedInBizClassLoader should be BizClassLoader");
    }
}
