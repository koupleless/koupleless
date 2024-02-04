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
package com.alipay.sofa.rpc.dubbo3.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author syd
 * @version DemoRequest.java, v 0.1 2023年11月05日 15:17 syd
 */
@Getter
@Setter
public class CommonRequest implements Serializable {
    private static final long serialVersionUID = 3378118426779776009L;
    private String            name;
}