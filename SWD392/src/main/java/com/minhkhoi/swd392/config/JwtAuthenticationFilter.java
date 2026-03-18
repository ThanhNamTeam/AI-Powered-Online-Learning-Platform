package com.minhkhoi.swd392.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhkhoi.swd392.dto.JwtInfo;
import com.minhkhoi.swd392.dto.response.ApiResponse;
import com.minhkhoi.swd392.repository.RedisTokenRepository;
import com.minhkhoi.swd392.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RedisTokenRepository redisTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String origin = request.getHeader("Origin");

        log.info("[JWT-FILTER] >>> {} {} | Origin: {}", method, uri, origin);

        final String authHeader = request.getHeader("Authorization");

        // Không có token → tiếp tục chain (public endpoints sẽ được permitAll xử lý)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JWT-FILTER] No Bearer token → passing to next filter | {} {}", method, uri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            JwtInfo info = jwtService.parseJwtInfo(jwt);
            String jti = info.getJwtId();

            boolean revoked = redisTokenRepository.existsById(jti);
            if (revoked) {
                log.warn("[JWT-FILTER] Token revoked for jti={} | {} {}", jti, method, uri);
                handleException(response, "Token has been revoked", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = jwtService.extractUsername(jwt);
            log.info("[JWT-FILTER] Token valid for user={} | {} {}", username, method, uri);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JWT-FILTER] Authentication set for user={} | {} {}", username, method, uri);
                } else {
                    log.warn("[JWT-FILTER] Token invalid for user={} | {} {}", username, method, uri);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("[JWT-FILTER] Token expired | {} {} | {}", method, uri, e.getMessage());
            handleException(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.warn("[JWT-FILTER] Malformed token | {} {} | {}", method, uri, e.getMessage());
            handleException(response, "Invalid token format", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (SignatureException e) {
            log.warn("[JWT-FILTER] Invalid signature | {} {} | {}", method, uri, e.getMessage());
            handleException(response, "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            log.error("[JWT-FILTER] Unexpected error | {} {} | {}", method, uri, e.getMessage(), e);
            handleException(response, "Token authentication failed: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleException(HttpServletResponse response, String message, int statusCode) throws IOException {
        log.warn("[JWT-FILTER] Returning {} | message={}", statusCode, message);
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.error(message);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
