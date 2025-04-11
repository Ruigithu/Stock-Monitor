package com.ruipeng.StockMonitor.service;

import com.ruipeng.StockMonitor.model.StockData;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long timeoutMinutes) {
        System.out.println("Setting cache: " + key + " with value: " + value);
        redisTemplate.opsForValue().set(key, value, timeoutMinutes, TimeUnit.MINUTES);
        Boolean success = redisTemplate.hasKey(key);
        System.out.println("Cache set success for " + key + ": " + success);
    }

    public StockData get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        System.out.println("Getting cache: " + key + ", value: " + value);
        return value != null ? (StockData) value : null;
    }
}
