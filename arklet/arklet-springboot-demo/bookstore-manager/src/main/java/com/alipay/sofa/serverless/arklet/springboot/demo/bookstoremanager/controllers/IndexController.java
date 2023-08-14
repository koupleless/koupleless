package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoremanager.controllers;


import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoremanager.data.DataSet;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.models.BookInfo;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.services.StrategyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 * 这里假设宿主应用提供商品展示的主业务模块
 */
@Controller
public class IndexController {
    @SofaReference
    private StrategyService strategyService;

    @RequestMapping("index")
    public String index(Model model) {
        model.addAttribute("bookList", strategyService.strategy(initBookList()));
        model.addAttribute("strategyName", strategyService.getStrategyName());
        return "index";
    }

    /**
     * 初始化默认按照索引展示列表，其中销量是乱序的
     *
     * @return List<BookInfo>
     */
    private List<BookInfo> initBookList() {
        List<BookInfo> bookList = new ArrayList<>();
        for (int i = 0; i < 5; i ++ ) {
            BookInfo bookInfo = new BookInfo();
            bookInfo.setName(DataSet.name[i]);
            bookInfo.setPrice(DataSet.price[i]);
            bookInfo.setOrderCount(DataSet.orderCount[i]);
            bookInfo.setImageUrl(DataSet.imageUrls[i]);
            bookInfo.setAuthor(DataSet.authors[i]);
            bookList.add(bookInfo);
        }
        return bookList;
    }
}
