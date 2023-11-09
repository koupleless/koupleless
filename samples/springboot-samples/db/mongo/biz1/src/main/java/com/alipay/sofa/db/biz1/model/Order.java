package com.alipay.sofa.db.biz1.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
public class Order {
    @MongoId
    private String id;

    private String title;

    private String content;
}
