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
package com.alipay.sofa.koupleless.arklet.core;

/**
 *  * Arklet component interface, managed by registry
 *  * @see ArkletComponentRegistry
 * @author mingmen
 * @date 2023/6/8
 */
public interface ArkletComponent {

    /**
     * ArkletComponent init method, called when arklet try to start
     * the extended custom component should use this method to do some initialization
     */
    void init();

    /**
     * ArkletComponent destroy method, called when arklet try to stop
     * the extended custom component should use this method to destroy itself
     */
    void destroy();
}
