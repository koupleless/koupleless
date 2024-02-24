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
package com.alipay.sofa.koupleless.common.service;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100003;
import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100004;
import static com.alipay.sofa.koupleless.common.service.ServiceProxyFactory.determineMostSuitableBiz;
import static com.alipay.sofa.koupleless.common.service.ServiceProxyFactory.getService;
import static com.alipay.sofa.koupleless.common.util.SerializeUtils.serializeTransform;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:57 下午
 */
public class SpringServiceInvoker implements MethodInterceptor {

    private static final Logger LOGGER          = LoggerFactory
                                                    .getLogger(SpringServiceInvoker.class);

    private static final String TOSTRING_METHOD = "toString";
    private static final String EQUALS_METHOD   = "equals";
    private static final String HASHCODE_METHOD = "hashCode";

    private Object              target;                                                    // 被调用方目标bean

    private String              name;

    private Class<?>            clientType;

    private String              bizName;                                                   // 被调用方bizName

    private String              bizVersion;                                                // 被调用方bizVersion

    private ClassLoader         clientClassLoader;                                         // 调用方classloader

    private ClassLoader         serviceClassLoader;                                        // 被调用方classloader

    public SpringServiceInvoker() {
    }

    public SpringServiceInvoker(Object target, String name, Class<?> clientType, String bizName,
                                String bizVersion, ClassLoader clientClassLoader,
                                ClassLoader serviceClassLoader) {
        this.target = target;
        this.name = name;
        this.clientType = clientType;
        this.bizName = bizName;
        this.bizVersion = bizVersion;
        this.clientClassLoader = clientClassLoader;
        this.serviceClassLoader = serviceClassLoader;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // todo bizVersion = "" 时
        Biz biz = determineMostSuitableBiz(bizName, bizVersion);
        if (biz == null) {
            throw new BizRuntimeException(E100003, String.format(
                "biz %s:%s does not exist when called", bizName, bizVersion));
        }
        if (BizState.ACTIVATED != biz.getBizState() && BizState.DEACTIVATED != biz.getBizState()) {
            throw new BizRuntimeException(E100004, String.format("biz %s:%s state %s is not valid",
                bizName, bizVersion, biz.getBizState()));
        }

        // delayed addressing
        if (this.target == null) {
            this.target = getService(bizName, bizVersion, name, clientType);
            if (this.target == null) {
                throw new BizRuntimeException(E100004, "Cannot find service bean from the biz "
                                                       + biz.getIdentity());
            }
            this.serviceClassLoader = this.target.getClass().getClassLoader();
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        long startTime = System.currentTimeMillis();
        try {
            Thread.currentThread().setContextClassLoader(serviceClassLoader);
            return doInvoke(invocation);
        } catch (Throwable e) {
            doCatch(invocation, e, startTime);
            throw e;
        } finally {
            doFinally(invocation, startTime);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected Object doInvoke(MethodInvocation invocation) throws InvocationTargetException,
                                                          IllegalAccessException {
        if (isCrossClassLoader(invocation)) {
            return invokeServiceCrossClassLoader(invocation);
        }
        return invokeService(invocation);
    }

    protected void doCatch(MethodInvocation invocation, Throwable e, long startTime) {
        // log
    }

    protected void doFinally(MethodInvocation invocation, long startTime) {
        // log、metrics
    }

    private boolean isCrossClassLoader(MethodInvocation invocation) {
        return target.getClass().getClassLoader() != invocation.getMethod().getDeclaringClass()
            .getClassLoader();
    }

    private Object invokeServiceCrossClassLoader(MethodInvocation invocation)
                                                                             throws InvocationTargetException,
                                                                             IllegalAccessException {
        Method clientMethod = invocation.getMethod();
        Object[] clientMethodArguments = invocation.getArguments();
        Class[] clientMethodArgumentTypes = clientMethod.getParameterTypes();

        if (TOSTRING_METHOD.equalsIgnoreCase(clientMethod.getName())
            && clientMethodArgumentTypes.length == 0) {
            return target.toString();
        } else if (EQUALS_METHOD.equalsIgnoreCase(clientMethod.getName())
                   && clientMethodArgumentTypes.length == 1) {
            return target.equals(clientMethodArguments[0]);
        } else if (HASHCODE_METHOD.equalsIgnoreCase(clientMethod.getName())
                   && clientMethodArgumentTypes.length == 0) {
            return target.hashCode();
        }

        Object[] serviceMethodArguments = (Object[]) serializeTransform(clientMethodArguments,
            serviceClassLoader);
        Class[] serviceMethodArgumentTypes = (Class[]) serializeTransform(
            clientMethodArgumentTypes, serviceClassLoader);
        Method serviceMethod = getTargetMethod(clientMethod, serviceMethodArgumentTypes);
        Object retVal = serviceMethod.invoke(target, serviceMethodArguments);
        return serializeTransform(retVal, clientClassLoader);
    }

    private Object invokeService(MethodInvocation invocation) throws InvocationTargetException,
                                                             IllegalAccessException {
        Method method = invocation.getMethod();
        //the method may be not public
        method.setAccessible(true);
        return method.invoke(target, invocation.getArguments());
    }

    private Method getTargetMethod(Method method, Class<?>[] argumentTypes) {
        try {
            return target.getClass().getMethod(method.getName(), argumentTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(target + " in " + bizName + ":" + bizVersion
                                            + " don't have the method " + method);
        }
    }
}
