package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreorderprovider.controllers;

import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreorderprovider.data.DataSet;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.models.BookInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 * 这里负责演示在宿主应用基础上添加新的业务模块（由子应用自主负责从网页路由开始搭建）
 */
@Controller
@RequestMapping("api-order")
public class OrderController {
    @RequestMapping("index")
    public String master(Model model){
        model.addAttribute("bookList", initBookList());
        return "order-index";
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
