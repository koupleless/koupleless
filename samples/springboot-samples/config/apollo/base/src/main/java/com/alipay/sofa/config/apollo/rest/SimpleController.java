package com.alipay.sofa.config.apollo.rest;

import com.alipay.sofa.config.apollo.config.DataConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    @Autowired
    private DataConfig dataConfig;

    @GetMapping("/getValue")
    public String getValue() {
        return dataConfig.getName();
    }
}
