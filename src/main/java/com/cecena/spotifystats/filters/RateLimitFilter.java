package com.cecena.spotifystats.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, AtomicInteger> requestCount = new ConcurrentHashMap<>();
    private final int MAX_REQUEST_PER_MINUTE = 50;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        AtomicInteger count = requestCount.computeIfAbsent(clientIp, k -> new AtomicInteger(0));

        if (count.incrementAndGet() >= MAX_REQUEST_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedRate = 60000)
    public void resetCounts() {
        requestCount.clear();
    }
}
