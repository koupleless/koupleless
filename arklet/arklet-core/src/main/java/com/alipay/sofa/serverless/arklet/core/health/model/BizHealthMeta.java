package com.alipay.sofa.serverless.arklet.core.health.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class BizHealthMeta {

    private String  bizName;

    private String  bizVersion;

    private BizState bizState;

    private String webContextPath;

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
