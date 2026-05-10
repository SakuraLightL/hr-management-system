package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.entity.Employee;
import com.portfolio.hr_system.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // 一覧（検索対応）
    @GetMapping
    public String list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("id").descending());

        Page<EmployeeDto> employeePage =
                service.searchPage(name, pageable);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("name", name);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        return "employees";
    }

    // 新規作成画面
    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute("employee", new Employee());

        return "employee-form";
    }

    // 保存（新規・更新共通）
    @PostMapping("/save")
    public String save(@ModelAttribute EmployeeDto dto) {

        service.saveFromDto(dto);

        return "redirect:/employees";
    }

    // 編集画面
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model) {

        EmployeeDto dto = service.findDtoById(id);

        model.addAttribute("employee", dto);

        return "employee-form";
    }

    // 削除
    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/employees";
    }
}