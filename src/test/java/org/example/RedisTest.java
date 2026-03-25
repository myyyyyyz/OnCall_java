package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Redis 连接测试
 */
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnectAndReadWrite() {
        // 1. 写入一个 key
        String key = "test:mykey";
        String value = "hello_redis";
        redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);

        // 2. 读取这个 key
        String result = (String) redisTemplate.opsForValue().get(key);

        // 3. 打印结果
        System.out.println("Redis 写入成功");
        System.out.println("Redis 读取结果：" + result);

        // 4. 判断是否成功
        if (value.equals(result)) {
            System.out.println("✅ Redis 连接正常，读写正常！");
        } else {
            System.out.println("❌ Redis 读写异常");
        }
    }
}
