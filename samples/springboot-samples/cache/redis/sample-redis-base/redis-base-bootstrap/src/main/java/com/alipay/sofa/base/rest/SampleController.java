package com.alipay.sofa.base.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @className: SampleController
 * @author: yuanyuan
 * @description:
 * @date: 2023/6/29 8:47 下午
 */
@RestController
public class SampleController {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Assert.notNull(redisTemplate);
        Assert.notNull(stringRedisTemplate);

        // 添加
        redisTemplate.opsForValue().set("name","testname");
        // 查询
        System.out.println(redisTemplate.opsForValue().get("name"));
        // 删除
        redisTemplate.delete("name");
        // 更新
        redisTemplate.opsForValue().set("name","testname111");
        // 查询
        System.out.println(redisTemplate.opsForValue().get("name"));

        // 添加
        stringRedisTemplate.opsForValue().set("name","testname222");
        // 查询
        System.out.println(stringRedisTemplate.opsForValue().get("name"));
        // 删除
        stringRedisTemplate.delete("name");
        // 更新
        stringRedisTemplate.opsForValue().set("name","testname333");
        // 查询
        System.out.println(stringRedisTemplate.opsForValue().get("name"));

        return "hello to ark master biz";
    }
}
