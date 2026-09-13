package com.portfolio.hr_system.service;
import com.portfolio.hr_system.entity.*;
import com.portfolio.hr_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOidcUserServiceTest {
    UserRepository repository = mock(UserRepository.class);
    CustomOidcUserService service = new CustomOidcUserService(repository);
    OidcUser oidc(Boolean verified) {
        Map<String,Object> claims = new HashMap<>(); claims.put("sub", "subject-123"); claims.put("email", "allowed@example.test");
        if (verified != null) claims.put("email_verified", verified);
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("OIDC_USER")),
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(600), claims));
    }
    User provisioned() {
        User user = new User(); user.setUsername("stable-login"); user.setProvider(AuthProvider.GOOGLE); user.setRole(Role.ADMIN);
        when(repository.findByEmail("allowed@example.test")).thenReturn(Optional.of(user)); return user;
    }
    @Test void unverifiedAndMissingVerificationAreDeniedBeforeDatabaseAccess() {
        for (Boolean value : Arrays.asList(false, null))
            assertThatThrownBy(() -> service.authorize(oidc(value))).isInstanceOf(OAuth2AuthenticationException.class);
        verifyNoInteractions(repository);
    }
    @Test void unknownAccountsAreNotRegistered() {
        assertThatThrownBy(() -> service.authorize(oidc(true))).isInstanceOf(OAuth2AuthenticationException.class);
        verify(repository, never()).save(any());
    }
    @Test void localAccountCannotBeLinkedByEmail() {
        provisioned().setProvider(AuthProvider.LOCAL);
        assertThatThrownBy(() -> service.authorize(oidc(true))).isInstanceOf(OAuth2AuthenticationException.class);
        verify(repository, never()).save(any());
    }
    @Test void verifiedProvisionedAccountRetainsNameAndDatabaseRole() {
        User account = provisioned(); OidcUser principal = service.authorize(oidc(true));
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(account.getUsername()).isEqualTo("stable-login");
        assertThat(account.getProviderId()).isEqualTo("subject-123");
    }
    @Test void changedSubjectIsRejected() {
        provisioned().setProviderId("another-subject");
        assertThatThrownBy(() -> service.authorize(oidc(true))).isInstanceOf(OAuth2AuthenticationException.class);
        verify(repository, never()).save(any());
    }
}
