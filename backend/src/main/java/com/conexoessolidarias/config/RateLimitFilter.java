package com.conexoessolidarias.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private static class Window {
        final AtomicLong start = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean sensitive = isSensitive(path, method);
        if (!sensitive) {
            chain.doFilter(request, response);
            return;
        }
        String key = request.getRemoteAddr() + ":" + path;
        Window w = windows.computeIfAbsent(key, k -> new Window());
        long now = System.currentTimeMillis();
        long start = w.start.get();
        if (now - start > WINDOW_MS) {
            w.start.set(now);
            w.count.set(1);
            chain.doFilter(request, response);
            return;
        }
        int c = w.count.incrementAndGet();
        if (c > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Muitas requisições. Tente novamente em instantes.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isSensitive(String path, String method) {
        if (path.startsWith("/api/v1/auth/login")) return true;
        if (path.startsWith("/api/v1/auth/forgot-password")) return true;
        if (path.startsWith("/api/v1/auth/register")) return true;
        if ("POST".equals(method) && "/api/v1/contact".equals(path)) return true;
        return false;
    }
}
