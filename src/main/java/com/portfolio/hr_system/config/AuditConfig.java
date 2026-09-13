package com.portfolio.hr_system.config;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import java.util.Optional;

@Configuration @EnableJpaAuditing
public class AuditConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return Optional.of(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                    ? "system" : auth.getName());
        };
    }
}
