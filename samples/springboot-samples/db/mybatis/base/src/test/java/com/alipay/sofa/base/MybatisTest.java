package com.alipay.sofa.base;

import com.alipay.sofa.base.mapper.UserMapper;
import com.alipay.sofa.base.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author: yuanyuan
 * @date: 2023/10/18 5:34 下午
 */
@SpringBootTest
@Slf4j
public class MybatisTest {

    static {
        System.setProperty("sofa.ark.embed.enable", "true");
        System.setProperty("sofa.ark.plugin.export.class.enable", "true");
    }

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsert() {
        userMapper.insert(User.builder().age(18).name("沉默王二").password("123456").build());
        userMapper.insert(User.builder().age(18).name("沉默王三").password("123456").build());
        userMapper.insert(User.builder().age(18).name("沉默王四").password("123456").build());
        log.info("查询所有：{}",userMapper.getAll().stream().toArray());
    }

    @Test
    void testQuery() {
        List<User> all = userMapper.getAll();
        log.info("查询所有：{}",all.stream().toArray());
    }

    @Test
    void testUpdate() {
        User one = userMapper.getOne(1);
        log.info("更新前{}", one);
        one.setPassword("654321");
        userMapper.update(one);
        log.info("更新后{}", userMapper.getOne(1));
    }

    @Test
    void testDelete() {
        log.info("删除前{}", userMapper.getAll().toArray());
        userMapper.delete(1);
        log.info("删除后{}", userMapper.getAll().toArray());

    }
}
