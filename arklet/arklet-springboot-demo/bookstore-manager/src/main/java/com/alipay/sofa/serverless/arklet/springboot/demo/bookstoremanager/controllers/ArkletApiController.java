package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoremanager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Lunarscave
 *
 */
@Controller
public class ArkletApiController {
    @RequestMapping("arklet-api-quick-start")
    public String arkletApiQuickStart() {
        return "arklet-api-quick-start";
    }
}
