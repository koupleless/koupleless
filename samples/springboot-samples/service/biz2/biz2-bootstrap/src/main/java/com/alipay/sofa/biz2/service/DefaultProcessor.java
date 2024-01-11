package com.alipay.sofa.biz2.service;

import com.alipay.sofa.biz2.facade.Data;
import com.alipay.sofa.biz2.facade.Request;
import com.alipay.sofa.biz2.facade.Processor;
import com.alipay.sofa.biz2.facade.Response;
import org.springframework.stereotype.Service;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 3:27 下午
 */
@Service
public class DefaultProcessor implements Processor {

    @Override
    public Response process(Request request) {
        Response response = new Response();
        response.setSuccess(true);
        Data data = new Data();
        data.setContent("biz2 defaultProcessor");
        response.setDate(data);
        return response;
    }
}
