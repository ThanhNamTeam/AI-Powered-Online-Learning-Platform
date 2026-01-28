package com.minhkhoi.swd392.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource; // Lưu ý import Interface này

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() { // Đổi tên hàm và kiểu trả về
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        // Chỉ định rõ Frontend của bạn (React)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);


        return source;
    }
}