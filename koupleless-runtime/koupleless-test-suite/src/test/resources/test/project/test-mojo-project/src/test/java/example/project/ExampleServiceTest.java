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
package example.project;

import example.project.base.ExampleService;
import org.junit.jupiter.api.*;

/**
 * @author CodeNoobKing
 * @date 2024/1/15
 */
public class ExampleServiceTest {
    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAllExecuted");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAllExecuted");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEachExecuted");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("afterEachExecuted");
    }

    @Test
    public void testInBizClassLoader() {
        ExampleService mockService = new ExampleService();
        mockService.greeting();
    }

    @Test
    public void testInBizClassLoaderFailed() {
        Assertions.fail("force fail");
    }
}
