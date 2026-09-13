package com.portfolio.hr_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    private final boolean googleEnabled;

    public LoginController(org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> registrations) {
        this.googleEnabled = registrations.getIfAvailable() != null;
    }

    @GetMapping("/login")
    public String login(org.springframework.ui.Model model) {
        model.addAttribute("googleEnabled", googleEnabled);
        return "login";
    }
}
