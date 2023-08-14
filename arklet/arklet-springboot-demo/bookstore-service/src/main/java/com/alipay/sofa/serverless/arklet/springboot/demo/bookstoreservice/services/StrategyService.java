package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.services;

import com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.models.BookInfo;
import java.util.List;

/**
 * 对传入的书本列表进行排序并返回
 *
 * @author Lunarscave
 */
public interface StrategyService {
    /**
    * get strategy and return bookList
    */
    List<BookInfo> strategy(List<BookInfo> books);


    String getStrategyName();
}
