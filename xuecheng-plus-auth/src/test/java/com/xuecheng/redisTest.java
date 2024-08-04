package com.xuecheng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
public class redisTest {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    public void test(){
        redisTemplate.opsForValue().set("age","18");
    }

}
