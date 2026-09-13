package com.hnv.elearning.security.oauth2;

import com.hnv.elearning.feature.auth.service.AuthenticationService;
import com.hnv.elearning.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOidcUserService extends OidcUserService {
    private final AuthenticationService authenticationService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest); //deaultOauth2UserService sẽ gửi 1 http req tới google nếu cấu hình userInfoUri
        log.info("OidcUser đã nhận được từ việc đổi secret + authorization code: {}", oidcUser);

        //luu db
        try{
            String email = oidcUser.getEmail();
            String fullName = oidcUser.getFullName();
            String sub = oidcUser.getSubject();
            String ava = oidcUser.getPicture();

            User user = authenticationService.processGoogleLogin(email, fullName, ava, sub);

            return new CustomOidcUser(oidcUser, user); //load vao securityContext

            // Catch rộng hơn (catch (Exception ex)) vì đây là ở tầng filter, global ko bắt
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("processing_error"), exception); //để CustomFailure bắt
        }
    }
}
