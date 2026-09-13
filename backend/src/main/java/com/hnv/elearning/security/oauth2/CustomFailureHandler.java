package com.hnv.elearning.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws  IOException, ServletException {
        String code = resolveErrorCode(exception);
        response.sendRedirect("http://localhost:5173/login?oauth_error=" + code);
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if(exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errodeCode = oauth2Exception.getError().getErrorCode();
            if("access_denied".equals(errodeCode)) {
                return "access_denied";
            }
        }
        return "login_failed"; // lỗi khác(config sai, DB lỗi, network,..) gộp chung
    }
}
