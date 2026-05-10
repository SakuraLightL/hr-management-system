package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.entity.Employee;
import com.portfolio.hr_system.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("id").descending());

        Page<EmployeeDto> employeePage =
                service.searchPage(name, pageable);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("name", name);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("loginUser", getLoginUserName(authentication));

        return "employees";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("employee", new EmployeeDto());
        return "employee-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute EmployeeDto dto) {
        service.saveFromDto(dto);
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        EmployeeDto dto = service.findDtoById(id);
        model.addAttribute("employee", dto);
        return "employee-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/employees";
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