package com.alipay.sofa.serverless.arklet.springboot.actuator.info.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class BizModel {

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

    public static BizModel createBizModel(Biz biz) {
        AssertUtil.assertNotNull(biz, "can not find biz");
        BizModel bizModel = createBizModel(biz.getBizName(), biz.getBizVersion());
        bizModel.bizState = biz.getBizState();
        bizModel.webContextPath = biz.getWebContextPath();
        return bizModel;
    }

    public static BizModel createBizModel(String bizName, String bizVersion) {
        BizModel bizModel = new BizModel();
        bizModel.bizName = bizName;
        bizModel.bizVersion = bizVersion;
        return bizModel;
    }

    public static List<BizModel> createBizModelList(List<Biz> bizList) {
        List<BizModel> bizModelList = new ArrayList<>();
        for (Biz biz : bizList) {
            bizModelList.add(createBizModel(biz));
        }
        return bizModelList;
    }

}
