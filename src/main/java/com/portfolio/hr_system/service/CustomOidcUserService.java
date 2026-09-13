package com.portfolio.hr_system.service;

import com.portfolio.hr_system.entity.AuthProvider;
import com.portfolio.hr_system.entity.User;
import com.portfolio.hr_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        if (!"google".equals(request.getClientRegistration().getRegistrationId())) {
            throw new OAuth2AuthenticationException("provider_not_allowed");
        }
        return authorize(super.loadUser(request));
    }

    // Only administrator-provisioned Google accounts may sign in. Never link local accounts by email.
    OidcUser authorize(OidcUser oidc) {
        String email = oidc.getEmail();
        if (!Boolean.TRUE.equals(oidc.getEmailVerified()) || email == null || email.isBlank()
                || oidc.getSubject() == null || oidc.getSubject().isBlank()) {
            throw new OAuth2AuthenticationException("verified_email_required");
        }
        User user = userRepository.findByEmail(email)
                .filter(account -> account.getProvider() == AuthProvider.GOOGLE)
                .orElseThrow(() -> new OAuth2AuthenticationException("account_not_allowed"));
        if (user.getProviderId() != null && !user.getProviderId().equals(oidc.getSubject())) {
            throw new OAuth2AuthenticationException("subject_mismatch");
        }
        user.setProviderId(oidc.getSubject());
        userRepository.save(user);
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oidc.getIdToken(), oidc.getUserInfo(), "sub");
    }
}
