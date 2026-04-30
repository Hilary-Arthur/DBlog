package com.example.dblog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceInterceptor.class);

    @Autowired
    private StringRedisTemplate redis;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Prevent browser caching of HTML pages so maintenance mode redirect works immediately
        if (isHtmlPage(path)) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        // Always allow admin pages, admin API, and maintenance status API
        if (path.equals("/admin.html") || path.startsWith("/api/admin/") || path.equals("/api/maintenance")) {
            return true;
        }

        // Allow static resources
        if (path.matches(".*\\.(css|js|png|svg|ico|jpg|jpeg|gif|woff|woff2|ttf|eot|map)$")) {
            return true;
        }

        boolean maintEnabled = isMaintenanceEnabled();

        // If user visits /maintenance.html but maintenance is OFF, redirect to index
        if (path.equals("/maintenance.html")) {
            if (!maintEnabled) {
                response.sendRedirect("/index.html");
                return false;
            }
            return true;
        }

        // For all other pages: if maintenance is ON, redirect to maintenance page
        if (maintEnabled) {
            response.sendRedirect("/maintenance.html");
            return false;
        }

        return true;
    }

    private boolean isHtmlPage(String path) {
        return path.endsWith(".html") || path.equals("/") || (!path.contains(".") && !path.startsWith("/api/"));
    }

    private boolean isMaintenanceEnabled() {
        try {
            String val = redis.opsForValue().get("dblog:maintenance");
            return "1".equals(val);
        } catch (Exception e) {
            log.warn("Failed to read maintenance status from Redis, defaulting to disabled: {}", e.getMessage());
            return false;
        }
    }
}
