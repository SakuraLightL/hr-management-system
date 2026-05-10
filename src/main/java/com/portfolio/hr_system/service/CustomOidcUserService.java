package com.portfolio.hr_system.service;

import com.portfolio.hr_system.entity.User;
import com.portfolio.hr_system.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log =
            LoggerFactory.getLogger(CustomOidcUserService.class);

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        Map<String, Object> attributes =
                oidcUser.getAttributes();

        String email =
                (String) attributes.get("email");

        String name =
                (String) attributes.get("name");

        String providerId =
                (String) attributes.get("sub");

        log.info(
                "Google login detected. email={}, name={}",
                email,
                name
        );

        User user = userRepository.findByEmail(email)
                .orElseGet(User::new);

        user.setUsername(name);
        user.setEmail(email);
        user.setProviderId(providerId);
        user.setProvider("GOOGLE");

        if (user.getRole() == null
                || user.getRole().isBlank()) {

            user.setRole("USER");
        }

        if (user.getPassword() == null) {
            user.setPassword("");
        }

        User savedUser = userRepository.save(user);

        String role = savedUser.getRole();

        log.info(
                "Login success. email={}, role={}",
                savedUser.getEmail(),
                role
        );

        return new DefaultOidcUser(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                ),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub"
        );
    }
}