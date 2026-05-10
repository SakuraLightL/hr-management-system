package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.entity.User;
import com.portfolio.hr_system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model,
                       Authentication authentication) {

        model.addAttribute("users", service.findAll());
        model.addAttribute("loginUser",
                getLoginUserName(authentication));

        return "users";
    }

    @GetMapping("/new")
    public String createForm(Model model,
                             Authentication authentication) {

        model.addAttribute("user", new User());
        model.addAttribute("loginUser",
                getLoginUserName(authentication));

        return "user-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute User user) {

        service.save(user);

        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       Model model,
                       Authentication authentication) {

        User user = service.findById(id);

        if (user == null) {
            return "redirect:/users";
        }

        model.addAttribute("user", user);

        model.addAttribute("loginUser",
                getLoginUserName(authentication));

        return "user-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/users";
    }

    private String getLoginUserName(Authentication authentication) {

        if (authentication == null) {
            return "未ログイン";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof DefaultOidcUser oidcUser) {

            String displayName = oidcUser.getFullName();

            if (displayName != null &&
                    !displayName.isBlank()) {

                return displayName;
            }

            String email = oidcUser.getEmail();

            if (email != null &&
                    !email.isBlank()) {

                return email;
            }
        }

        if (principal instanceof DefaultOAuth2User oauthUser) {

            String displayName =
                    oauthUser.getAttribute("name");

            if (displayName != null &&
                    !displayName.isBlank()) {

                return displayName;
            }

            String email =
                    oauthUser.getAttribute("email");

            if (email != null &&
                    !email.isBlank()) {

                return email;
            }
        }

        return authentication.getName();
    }
}