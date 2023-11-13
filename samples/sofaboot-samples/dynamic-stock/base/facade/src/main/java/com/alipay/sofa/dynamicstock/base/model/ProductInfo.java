package com.alipay.sofa.dynamicstock.base.model;

/**
 * 商品信息类
 *
 * @author caojie.cj@antfin.com
 * @since 2020/2/11
 */
public class ProductInfo {
    private String     name;
    private String     author;
    private String     src;
    private Integer    orderCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }
}
