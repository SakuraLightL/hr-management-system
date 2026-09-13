package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.repository.DepartmentRepository;
import com.portfolio.hr_system.repository.EmployeeRepository;
import com.portfolio.hr_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
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

        return "dashboard";
    }

}
