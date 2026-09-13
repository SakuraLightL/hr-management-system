package com.portfolio.hr_system.controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {
    @ModelAttribute("loginUser")
    public String loginUser(Authentication authentication) {
        if (authentication == null) return "未ログイン";
        if (authentication.getPrincipal() instanceof OidcUser oidc) {
            if (oidc.getFullName() != null && !oidc.getFullName().isBlank()) return oidc.getFullName();
            if (oidc.getEmail() != null) return oidc.getEmail();
        }
        return authentication.getName();
    }
}
