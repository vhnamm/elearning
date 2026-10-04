package com.hnv.elearning.security.jwt;

import com.hnv.elearning.common.utils.HeaderUtil;
import com.hnv.elearning.infrastructure.redis.RedisService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailService customUserDetailService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("start jwt filter");
        String bearerToken = HeaderUtil.extractBearerToken(request);
        if (bearerToken == null) {
            filterChain.doFilter(request, response);
            return;
        }


        try {
            Claims claims = jwtProvider.parseClaims(bearerToken);
            if(redisService.exists("auth:blacklist:" + claims.getId())){
                log.info("access token in blacklist");
                filterChain.doFilter(request, response);
                return;
            }
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(authentication.getDetails());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (io.jsonwebtoken.JwtException e) {
            log.info("Invalid/expired JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private static final java.util.Set<String> PUBLIC_AUTH_PATHS = java.util.Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/confirm-otp",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/logout"
    );

    // GET công khai (khớp permitAll trong SecurityConfig, không dùng thông tin user) nên khỏi load user từ DB.
    private static final java.util.regex.Pattern PUBLIC_GET_PATHS = java.util.regex.Pattern.compile(
            "/api/v1/courses(/popular|/\\d+|/\\d+/curriculum)?|/api/v1/(search|categories)(/.*)?"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (PUBLIC_AUTH_PATHS.contains(uri)) {
            return true;
        }
        return "GET".equals(request.getMethod()) && PUBLIC_GET_PATHS.matcher(uri).matches();
    }
}
