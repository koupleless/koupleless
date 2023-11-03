package com.alipay.sofa.dynamicstock.biz1.impl;

import com.alipay.sofa.dynamicstock.base.facade.StrategyService;
import com.alipay.sofa.dynamicstock.base.model.ProductInfo;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 对传入的商品列表进行排序实现类
 *
 * @author caojie.cj@antfin.com
 * @since 2020/2/11
 */
@Service
@SofaService
public class StrategyServiceImpl implements StrategyService {
    @Override
    public List<ProductInfo> strategy(List<ProductInfo> products) {
//        Collections.sort(products, (m, n) -> n.getOrderCount() - m.getOrderCount());
//        products.stream().forEach(p -> p.setName(p.getName()+"("+p.getOrderCount()+")"));
        return products;
    }
}
