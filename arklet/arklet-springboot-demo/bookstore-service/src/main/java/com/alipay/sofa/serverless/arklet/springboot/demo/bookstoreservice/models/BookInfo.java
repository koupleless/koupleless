package com.alipay.sofa.serverless.arklet.springboot.demo.bookstoreservice.models;

public class BookInfo {
    private String     name;
    private String     author;
    private String     imageUrl;
    private Double     price;
    private Integer    orderCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {return price; }

    public void setPrice(Double price) {this.price = price;}

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String src) {
        this.imageUrl = src;
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
