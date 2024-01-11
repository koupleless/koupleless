package com.alipay.sofa.biz.service;

import com.alipay.sofa.biz.facade.Param;
import com.alipay.sofa.biz.facade.Provider;
import com.alipay.sofa.biz.facade.Result;
import com.alipay.sofa.biz.facade.Teacher;
import org.springframework.stereotype.Service;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 3:27 下午
 */
@Service
public class TeacherProvider implements Provider {

    @Override
    public Result provide(Param param) {
        Result result = new Result();
        result.setSuccess(true);
        result.setPeople(new Teacher());
        return result;
    }
}
