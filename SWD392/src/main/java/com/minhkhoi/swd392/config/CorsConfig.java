package com.minhkhoi.swd392.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // Nhận FRONTEND_URL từ env, fallback về localhost cho dev
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // Cho phép thêm origins khác nếu cần (VD: Render preview URLs)
    @Value("${app.extra-allowed-origins:}")
    private String extraAllowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Restrict origins thay vì dùng wildcard "*"
        List<String> allowedOrigins = new java.util.ArrayList<>();
        allowedOrigins.add(frontendUrl);

        // Thêm localhost cho development nếu cần
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:5173");  // Vite dev server

        // Thêm extra origins từ env nếu có
        if (extraAllowedOrigins != null && !extraAllowedOrigins.isBlank()) {
            Arrays.stream(extraAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(allowedOrigins::add);
        }

        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L); // Cache preflight 1 giờ để giảm OPTIONS requests

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}