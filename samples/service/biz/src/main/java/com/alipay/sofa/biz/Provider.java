package com.alipay.sofa.biz;

import com.alipay.sofa.biz.model.Param;
import com.alipay.sofa.biz.model.Result;

public interface Provider {

    Result provide(Param param);
}
