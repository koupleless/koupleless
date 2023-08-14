package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreprovider.serviceImpl;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.models.BookInfo;
import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.services.StrategyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lunarscave
 */
@Service
@SofaService
public class StrategyServiceImpl implements StrategyService {
    @Override
    public List<BookInfo> strategy(List<BookInfo> bookList) {
        // descending order
//        bookList.sort((i, j) -> (int) (j.getPrice() - i.getPrice()));

        // ascending order
        bookList.sort((i, j) -> (int) (i.getPrice() - j.getPrice()));
        return bookList;
    }

    @Override
    public String getStrategyName() {
        // descending order
//        return "按价格降序";

        // ascending order
        return "按价格升序";
    }
}
