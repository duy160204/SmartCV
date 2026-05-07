package com.example.SmartCV.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GlobalRateLimitFilter implements Filter {

    // Eviction Policy: Cap at 5000 IPs to prevent memory leak (LRU Cache)
    private final Map<String, AtomicInteger> localLimiter = Collections.synchronizedMap(
            new LinkedHashMap<String, AtomicInteger>(5000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AtomicInteger> eldest) {
                    return size() > 5000;
                }
            });

    private final Map<String, Long> localTimer = Collections.synchronizedMap(
            new LinkedHashMap<String, Long>(5000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                    return size() > 5000;
                }
            });

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String clientIp = getClientIp(req);
        long now = Instant.now().getEpochSecond();
        long currentMin = now / 60;

        localTimer.putIfAbsent(clientIp, currentMin);
        if (!localTimer.get(clientIp).equals(currentMin)) {
            localTimer.put(clientIp, currentMin);
            localLimiter.put(clientIp, new AtomicInteger(50)); // Match capacity: 50 requests/min
        }

        localLimiter.putIfAbsent(clientIp, new AtomicInteger(50));
        int remaining = localLimiter.get(clientIp).decrementAndGet();

        if (remaining < 0) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.getWriter().write("Too Many Requests. Please slow down.");
            return;
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
