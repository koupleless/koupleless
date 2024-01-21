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
package com.alipay.sofa.koupleless.arklet.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lunarscave
 */
public class ConvertUtils {

    /**
     * convert bytes(B) to megabyte(MB)
     * @param bytes input byte(B) value
     * @return megabyte(MB)
     */
    public static double bytes2Megabyte(Long bytes) {
        return ((double) bytes) / 1024 / 1024;
    }

    /**
     * get duration from param date till now and change to second(s)
     * @param date input date
     * @return output duration(s)
     */
    public static double getDurationSecond(Date date) {
        return ((double) System.currentTimeMillis() - date.getTime()) / 1000;
    }

}
