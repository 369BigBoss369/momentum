package com.momentum.security;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.AuthProvider;
import com.momentum.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        OidcUser oidcUser = super.loadUser(userRequest);

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        log.debug("Processing OIDC user - provider: GOOGLE, providerId: {}", providerId);

        User user = userService.createOrGetOAuth2User(AuthProvider.GOOGLE, providerId, email, name);

        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_disabled"), "This account has been deactivated.");
        }

        return new CustomOidcUser(oidcUser, user);
    }
}