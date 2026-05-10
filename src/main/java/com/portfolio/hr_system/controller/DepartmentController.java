package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.entity.Department;
import com.portfolio.hr_system.service.DepartmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", service.findAll());
        return "departments";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("department", new Department());
        return "department-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Department department) {
        service.save(department);
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("department", service.findById(id));
        return "department-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/departments";
    }
}