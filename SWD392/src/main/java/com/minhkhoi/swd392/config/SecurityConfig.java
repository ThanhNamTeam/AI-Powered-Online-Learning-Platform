package com.minhkhoi.swd392.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Authentication not required
                        .requestMatchers(
                                "/api/auth/send-otp",
                                "/api/auth/login",
                                "/api/auth/refresh-token",
                                "/api/auth/validate-token",
                                "/api/accounts",
                                "/api/accounts/exists/**",
                                "/api/videos/**",  // Video upload endpoints (for testing)
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/accounts/forgot-password",
                                "/api/accounts/forgot-password",
                                "/api/accounts/reset-password",
                                "/momo-test.html",               // Payment Test Page
                                "/payment-result.html",          // Payment Result Page
                                "/api/identity/payment/momo/callback", // MOMO IPN Callback
                                "/api/identity/payment/momo/return",    // MOMO Return URL (redirect)
                                "/api/identity/payment/vnpay/callback", // VNPAY Callback
                                "/api/courses/all-course/public",        // Public Course List
                                "/api/placement-test/**"                  // Placement Test (Guest access)
                        ).permitAll()
                        // Staff endpoints - yêu cầu STAFF hoặc ADMIN role
                        .requestMatchers(
                                "/api/placement-documents/**"             // Quản lý tài liệu Placement Test
                        ).authenticated()
                        // Protected endpoints - Authentication required (use Authorize button in Swagger)
                        .requestMatchers(
                                "/api/accounts/me",
                                "/api/accounts/{userId}",
                                "/api/accounts/email/**"
                        ).authenticated()
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

