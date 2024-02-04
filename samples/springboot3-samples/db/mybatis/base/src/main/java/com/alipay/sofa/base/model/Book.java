package com.alipay.sofa.base.model;


import lombok.Data;

/**
 * @author: chaya
 * @date: 2023/10/19 5:01 下午
 */
@Data
public class Book {

    private Integer id;

    private String name;

    private int price;

    private String category;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
