package com.xuecheng.checkcode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void testRedisTemplate() {
        redisTemplate.opsForValue().set("testKey", "testValue", 10, TimeUnit.SECONDS);
        String value = redisTemplate.opsForValue().get("testKey");
        assertEquals("testValue", value);
    }
}

