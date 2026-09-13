package com.portfolio.hr_system.controller;
import com.portfolio.hr_system.dto.DepartmentDto;
import com.portfolio.hr_system.service.DepartmentService;
import com.portfolio.hr_system.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/departments") @RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService service;
    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id") Pageable pageable, Model model) {
        var page = service.findAll(pageable);
        model.addAttribute("departments", page.getContent()); model.addAttribute("page", page);
        return "departments";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("department", new DepartmentDto()); return "department-form";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("department", service.findById(id)); return "department-form";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("department") DepartmentDto dto, BindingResult errors) {
        if (errors.hasErrors()) return "department-form";
        try { service.save(dto); }
        catch (IllegalArgumentException | DuplicateResourceException ex) {
            errors.reject("invalid", ex.getMessage()); return "department-form";
        }
        return "redirect:/departments";
    }
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/departments"; }
}
