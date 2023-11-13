package com.alipay.sofa.db.base.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
public class CommonModel {
    @MongoId
    private String id;

    private String name;
}
