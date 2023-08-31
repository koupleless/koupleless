package com.alipay.sofa.serverless.arklet.core.actuator.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class BizHealthModel {

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

    public static BizHealthModel createBizModel(Biz biz) {
        AssertUtils.assertNotNull(biz, "can not find biz");
        BizHealthModel bizHealthModel = createBizModel(biz.getBizName(), biz.getBizVersion());
        bizHealthModel.bizState = biz.getBizState();
        bizHealthModel.webContextPath = biz.getWebContextPath();
        return bizHealthModel;
    }

    public static BizHealthModel createBizModel(String bizName, String bizVersion) {
        BizHealthModel bizHealthModel = new BizHealthModel();
        bizHealthModel.bizName = bizName;
        bizHealthModel.bizVersion = bizVersion;
        return bizHealthModel;
    }

    public static List<BizHealthModel> createBizModelList(List<Biz> bizList) {
        AssertUtils.isTrue(bizList.size() > 0, "no biz found");
        List<BizHealthModel> bizHealthModelList = new ArrayList<>();
        for (Biz biz : bizList) {
            bizHealthModelList.add(createBizModel(biz));
        }
        return bizHealthModelList;
    }

}
