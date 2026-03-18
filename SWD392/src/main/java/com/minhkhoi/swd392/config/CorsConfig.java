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

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        // Dùng AllowedOriginPatterns thay vì AllowedOrigins
        // để tương thích tốt hơn với credentials + wildcard subdomain
        config.setAllowedOriginPatterns(Arrays.asList(
                frontendUrl,                    // VD: https://swd392.netlify.app
                "http://localhost:3000",
                "http://localhost:5173",
                "https://*.netlify.app",         // Cho phép tất cả Netlify preview URLs
                "https://*.onrender.com"         // Cho phép Render preview nếu cần
        ));

        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}