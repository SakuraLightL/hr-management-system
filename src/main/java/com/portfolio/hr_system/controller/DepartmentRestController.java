package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.dto.ApiResponse;
import com.portfolio.hr_system.entity.Department;
import com.portfolio.hr_system.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentRestController {

    private final DepartmentService service;

    public DepartmentRestController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Department>> getAll() {

        List<Department> departments = service.findAll();

        return new ApiResponse<>(
                "success",
                "部署一覧取得成功",
                departments
        );
    }
}