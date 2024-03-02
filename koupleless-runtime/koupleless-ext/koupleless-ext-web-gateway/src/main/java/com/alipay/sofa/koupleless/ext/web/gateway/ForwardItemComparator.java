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
package com.alipay.sofa.koupleless.ext.web.gateway;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Comparator;

/**
 * Compares the priorities of two ForwardItem objects.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ForwardItemComparator implements Comparator<ForwardItem> {
    @Getter
    private static final Comparator<ForwardItem> instance = new ForwardItemComparator();

    /**
     * Compares the priorities of two ForwardItem objects.
     *
     * @param item1 The first ForwardItem object.
     * @param item2 The second ForwardItem object.
     * @return Returns a comparison result: if item1's host is empty and item2's host is not, returns 1;
     * if item1's host is not empty and item2's host is empty, returns -1;
     * otherwise, subtracts the length of item1's path from item2's path, and if the difference is 0,
     * subtracts the length of item1's host from item2's host to determine the result.
     */
    public int compare(ForwardItem item1, ForwardItem item2) {
        // Retrieve the host strings
        String host1 = item1.getHost();
        String host2 = item2.getHost();
        // Compare hosts: return 1 if item1's host is empty and item2's isn't, -1 otherwise
        if (!StringUtils.hasLength(host1) && StringUtils.hasLength(host2)) {
            return 1;
        }
        if (StringUtils.hasLength(host1) && !StringUtils.hasLength(host2)) {
            return -1;
        }
        // Compare path lengths; use Integer.compare for a cleaner comparison
        int num = Integer.compare(item2.getFrom().length(), item1.getFrom().length());
        if (num == 0) {
            num = Integer.compare(item2.getHost().length(), item1.getHost().length());
        }
        return num;
    }
}
