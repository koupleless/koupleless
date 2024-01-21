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
package com.alipay.sofa.koupleless.arklet.core.health.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class BizHealthMeta {

    private String   bizName;

    private String   bizVersion;

    private BizState bizState;

    private String   webContextPath;

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String getBizVersion() {
        return bizVersion;
    }

    public void setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
    }

    public BizState getBizState() {
        return bizState;
    }

    public void setBizState(BizState bizState) {
        this.bizState = bizState;
    }

    public String getWebContextPath() {
        return webContextPath;
    }

    public void setWebContextPath(String webContextPath) {
        this.webContextPath = webContextPath;
    }

    public static BizHealthMeta createBizMeta(Biz biz) {
        AssertUtils.assertNotNull(biz, "can not find biz");
        BizHealthMeta bizHealthMeta = createBizMeta(biz.getBizName(), biz.getBizVersion());
        bizHealthMeta.bizState = biz.getBizState();
        bizHealthMeta.webContextPath = biz.getWebContextPath();
        return bizHealthMeta;
    }

    public static BizHealthMeta createBizMeta(String bizName, String bizVersion) {
        BizHealthMeta bizHealthMeta = new BizHealthMeta();
        bizHealthMeta.bizName = bizName;
        bizHealthMeta.bizVersion = bizVersion;
        return bizHealthMeta;
    }

    public static List<BizHealthMeta> createBizMetaList(List<Biz> bizList) {
        AssertUtils.isTrue(bizList.size() > 0, "no biz found");
        List<BizHealthMeta> bizHealthMetaList = new ArrayList<>();
        for (Biz biz : bizList) {
            bizHealthMetaList.add(createBizMeta(biz));
        }
        return bizHealthMetaList;
    }

}
