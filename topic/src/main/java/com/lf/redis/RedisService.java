package com.lf.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //存缓存
    public void set(String key, Object value, Long timeOut) {
        redisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
    }

    //取缓存
    public Object get(String key) {
        return  redisTemplate.opsForValue().get(key);
    }

    //清除缓存
    public void del(String key) {
        redisTemplate.delete(key);
    }

    // 批量获取以某个前缀开头的所有键
    public List<Object> getKeysWithPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            return redisTemplate.opsForValue().multiGet(keys);
        }
        return null;
    }
}
