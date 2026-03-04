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
import org.springframework.data.redis.core.RedisTemplate;
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
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Skip JWT validation for public endpoints
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            JwtInfo info = jwtService.parseJwtInfo(jwt);
            String jti = info.getJwtId();

            // Check if the token is revoked
            boolean revoked = redisTokenRepository.existsById(jti);
            if (revoked) {
                handleException(response, "Token has been revoked", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            username = jwtService.extractUsername(jwt);

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
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleException(response, "Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            handleException(response, "Invalid token format", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (SignatureException e) {
            handleException(response, "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            handleException(response, "Token authentication failed: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleException(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.error(message);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}

