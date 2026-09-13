package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.repository.DepartmentRepository;
import com.portfolio.hr_system.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final DepartmentRepository departmentRepository;

    public EmployeeController(EmployeeService service,
                              DepartmentRepository departmentRepository) {
        this.service = service;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        Pageable pageable =
                PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), Sort.by("id").descending());

        Page<EmployeeDto> employeePage =
                service.searchPage(name, pageable);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("name", name);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        return "employees";
    }

    @GetMapping("/new")
    public String createForm(Model model,
                             Authentication authentication) {

        model.addAttribute("employee", new EmployeeDto());
        model.addAttribute("departments",
                departmentRepository.findAllByOrderByNameAsc());

        return "employee-form";
    }

    @PostMapping("/save")
    public String save(@jakarta.validation.Valid @ModelAttribute EmployeeDto dto) {

        service.saveFromDto(dto);

        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           Authentication authentication) {

        EmployeeDto dto = service.findDtoById(id);


        model.addAttribute("employee", dto);
        model.addAttribute("departments",
                departmentRepository.findAllByOrderByNameAsc());

        return "employee-form";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/employees";
    }

}
