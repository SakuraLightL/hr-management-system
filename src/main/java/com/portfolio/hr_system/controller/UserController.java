package com.portfolio.hr_system.controller;
import com.portfolio.hr_system.dto.UserDto;
import com.portfolio.hr_system.service.UserService;
import com.portfolio.hr_system.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/users") @RequiredArgsConstructor
public class UserController {
    private final UserService service;
    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id") Pageable pageable, Model model) {
        var page = service.findAll(pageable);
        model.addAttribute("users", page.getContent()); model.addAttribute("page", page);
        return "users";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new UserDto()); return "user-form";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("user", service.findById(id)); return "user-form";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("user") UserDto dto, BindingResult errors) {
        if (errors.hasErrors()) return "user-form";
        try { service.save(dto); }
        catch (IllegalArgumentException | DuplicateResourceException ex) {
            errors.reject("invalid", ex.getMessage()); return "user-form";
        }
        return "redirect:/users";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/users"; }
}
