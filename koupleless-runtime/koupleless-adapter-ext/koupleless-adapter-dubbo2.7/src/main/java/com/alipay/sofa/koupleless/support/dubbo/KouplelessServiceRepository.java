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
package com.alipay.sofa.koupleless.support.dubbo;

import org.apache.dubbo.common.context.FrameworkExt;
import org.apache.dubbo.common.extension.Wrapper;
import org.apache.dubbo.rpc.model.ServiceDescriptor;
import org.apache.dubbo.rpc.model.ServiceRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: yuanyuan
 * @date: 2023/12/25 9:07 下午
 */
@Wrapper(matches = { "repository" })
public class KouplelessServiceRepository extends ServiceRepository {

    // services
    private static ConcurrentMap<Class<?>, ServiceDescriptor> globalClassServices = new ConcurrentHashMap<>();

    private static ConcurrentMap<String, ServiceDescriptor>   globalPathServices  = new ConcurrentHashMap<>();

    private ServiceRepository                                 source;

    public KouplelessServiceRepository(FrameworkExt frameworkExt) {
        if (frameworkExt instanceof ServiceRepository) {
            this.source = (ServiceRepository) frameworkExt;
        }
    }

    @Override
    public ServiceDescriptor registerService(Class<?> interfaceClazz) {
        ServiceDescriptor serviceDescriptor = globalClassServices.computeIfAbsent(interfaceClazz,
                _k -> new ServiceDescriptor(interfaceClazz));
        globalPathServices.putIfAbsent(interfaceClazz.getName(), serviceDescriptor);
        return serviceDescriptor;
    }

    @Override
    public ServiceDescriptor registerService(String path, Class<?> interfaceClass) {
        ServiceDescriptor serviceDescriptor = registerService(interfaceClass);
        // if path is different with interface name, add extra path mapping
        if (!interfaceClass.getName().equals(path)) {
            globalPathServices.putIfAbsent(path, serviceDescriptor);
        }
        return serviceDescriptor;
    }

    @Override
    public void unregisterService(String path) {
        globalPathServices.remove(path);
    }

    @Override
    public List<ServiceDescriptor> getAllServices() {
        return Collections.unmodifiableList(new ArrayList<>(globalClassServices.values()));
    }

    @Override
    public ServiceDescriptor lookupService(String pathOrInterfaceName) {
        return globalPathServices.get(pathOrInterfaceName);
    }

    @Override
    public void destroy() throws IllegalStateException {
        // currently works for unit test
        globalClassServices.clear();
        globalPathServices.clear();
        super.destroy();
    }

}
