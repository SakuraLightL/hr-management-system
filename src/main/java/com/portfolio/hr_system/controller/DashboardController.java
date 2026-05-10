package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.repository.DepartmentRepository;
import com.portfolio.hr_system.repository.EmployeeRepository;
import com.portfolio.hr_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DashboardController(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Authentication authentication) {

        model.addAttribute("employeeCount", employeeRepository.count());
        model.addAttribute("departmentCount", departmentRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("loginUser", getLoginUserName(authentication));

        return "dashboard";
    }

    private String getLoginUserName(Authentication authentication) {

        if (authentication == null) {
            return "未ログイン";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof DefaultOidcUser oidcUser) {

            String displayName = oidcUser.getFullName();

            if (displayName != null && !displayName.isBlank()) {
                return displayName;
            }

            String email = oidcUser.getEmail();

            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        if (principal instanceof DefaultOAuth2User oauthUser) {

            String displayName = oauthUser.getAttribute("name");

            if (displayName != null && !displayName.isBlank()) {
                return displayName;
            }

            String email = oauthUser.getAttribute("email");

            if (email != null && !email.isBlank()) {
                return email;
            }
        }

        return authentication.getName();
    }
}