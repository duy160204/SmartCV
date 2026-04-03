package com.example.SmartCV.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalRateLimitFilter implements Filter {

    private final AtomicLong lastErrorLogTime = new AtomicLong(0);

    // Eviction Policy: Cap at 5000 IPs to prevent memory leak
    private final Map<String, AtomicInteger> localFallbackLimiter = Collections.synchronizedMap(
            new LinkedHashMap<String, AtomicInteger>(5000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AtomicInteger> eldest) {
                    return size() > 5000;
                }
            });

    private final Map<String, Long> localFallbackTimer = Collections.synchronizedMap(
            new LinkedHashMap<String, Long>(5000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                    return size() > 5000;
                }
            });

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LUA_SCRIPT =
        "local key = KEYS[1]\n" +
        "if not key then return -1 end\n" +
        "local capacity = tonumber(ARGV[1]) or 50\n" +
        "local rate = tonumber(ARGV[2]) or 10\n" +
        "local now = tonumber(ARGV[3]) or 0\n" +
        "local requested = tonumber(ARGV[4]) or 1\n" +
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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String clientIp = getClientIp(req);
        String key = "global_rate_limit:ip:" + clientIp;
        long now = Instant.now().getEpochSecond();
        
        long capacity = 50;
        long refillRate = 10;
        long requested = 1;

        log.debug("RateLimit ARGV -> capacity={}, refillRate={}, now={}", capacity, refillRate, now);

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key), 
                String.valueOf(capacity), String.valueOf(refillRate), String.valueOf(now), String.valueOf(requested));

            if (result == null || result == 0L) {
                log.warn("Global Rate Limit Exceeded for IP: {}", clientIp);
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.getWriter().write("Too Many Requests. Please slow down.");
                return;
            }
        } catch (Exception redisDown) {
            long currentMin = now / 60;
            if (lastErrorLogTime.getAndSet(currentMin) != currentMin) {
                log.error("Redis execution failure", redisDown);
            }

            // Local JVM fallback (simplified token bucket per minute)
            localFallbackTimer.putIfAbsent(clientIp, currentMin);
            if (!localFallbackTimer.get(clientIp).equals(currentMin)) {
                localFallbackTimer.put(clientIp, currentMin);
                localFallbackLimiter.put(clientIp, new AtomicInteger(50)); // Match capacity
            }

            localFallbackLimiter.putIfAbsent(clientIp, new AtomicInteger(50));
            int remaining = localFallbackLimiter.get(clientIp).decrementAndGet();
            
            if (remaining < 0) {
                if (lastErrorLogTime.getAndSet(currentMin + 1000) != currentMin + 1000) {
                    log.warn("Local Rate Limit Fallback Exceeded for IP: {}", clientIp);
                }
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.getWriter().write("Too Many Requests (Local Fallback). Please slow down.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
