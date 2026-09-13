package com.hnv.elearning.security.oauth2;

import com.hnv.elearning.common.utils.CookieUtil;
import com.hnv.elearning.common.utils.HeaderUtil;
import com.hnv.elearning.feature.auth.service.AuthenticationService;
import com.hnv.elearning.feature.auth.service.impl.RefreshTokenService;
import com.hnv.elearning.feature.user.entity.User;
import com.hnv.elearning.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    @Value("${jwt.refresh-expiration}")
    private Long REFRESH_EXPIRATION;

    @Value("${spring.application.frontend-url}")
    private String callbackUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomOidcUser principal = (CustomOidcUser) authentication.getPrincipal();
        User user = principal.getUser();

        String accessToken = jwtProvider.generateJwtToken(user);
        String refreshToken = refreshTokenService.create(user, HeaderUtil.getClientIp(request), HeaderUtil.getUserAgent(request));

        CookieUtil.addCookie(response,
                "refreshToken",
                refreshToken,
                "/api/v1/auth",
                REFRESH_EXPIRATION,
                true
        );

        String redirectUrl = callbackUrl + "oauth2/callback#accessToken=" + accessToken;
        response.sendRedirect(redirectUrl);

    }
}
