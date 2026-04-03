package com.example.SmartCV.modules.ai.service;

import java.time.Instant;
import java.util.Collections;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import com.example.SmartCV.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiRateLimiter {
    private final RedisTemplate<String, Object> redisTemplate;

    // True Token Bucket Algorithm deployed directly onto Redis via atomic Lua
    private static final String LUA_SCRIPT =
        "local key = KEYS[1]\n" +
        "local rate = tonumber(ARGV[1])\n" +
        "local capacity = tonumber(ARGV[2])\n" +
        "local now = tonumber(ARGV[3])\n" +
        "local requested = tonumber(ARGV[4])\n" +
        "local fill_time = capacity/rate\n" +
        "local ttl = math.floor(fill_time*2)\n" +
        "local last_tokens = tonumber(redis.call('hget', key, 'tokens'))\n" +
        "if last_tokens == nil then last_tokens = capacity end\n" +
        "local last_refreshed = tonumber(redis.call('hget', key, 'timestamp'))\n" +
        "if last_refreshed == nil then last_refreshed = 0 end\n" +
        "local delta = math.max(0, now - last_refreshed)\n" +
        "local filled_tokens = math.min(capacity, last_tokens + (delta * rate))\n" +
        "local allowed = filled_tokens >= requested\n" +
        "local new_tokens = filled_tokens\n" +
        "if allowed then new_tokens = filled_tokens - requested end\n" +
        "redis.call('hset', key, 'tokens', new_tokens)\n" +
        "redis.call('hset', key, 'timestamp', now)\n" +
        "redis.call('expire', key, ttl)\n" +
        "return allowed and 1 or 0";

    public void checkRateLimit(Long userId) {
        String key = "rate_limit:ai:tokens:" + userId;
        long now = Instant.now().getEpochSecond();
        // rate: 1 token per 60 secs (0.016), capacity: 5 burst requests. Rejects if empty.
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), 
            "0.0166", "5", String.valueOf(now), "1");

        if (result == null || result == 0L) {
            throw new BusinessException("AI Rate Limit Exceeded. Your Token Bucket is temporarily empty.", org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
