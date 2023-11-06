package com.alipay.sofa.db.biz2.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
public class User {
    @MongoId
    private String id;

    private String name;

    private int age;

    private String gender;
}
