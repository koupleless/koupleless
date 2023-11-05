package com.alipay.sofa.base.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @author: yuanyuan
 * @date: 2023/10/18 4:44 下午
 */
@Data
@Builder
public class User {

    private Integer id;
    private Integer age;
    private String name;
    private String password;

    @Tolerate
    User() {}
}
