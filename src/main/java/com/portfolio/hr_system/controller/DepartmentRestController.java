package com.portfolio.hr_system.controller;
import com.portfolio.hr_system.dto.*;
import com.portfolio.hr_system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/departments") @RequiredArgsConstructor
public class DepartmentRestController {
    private final DepartmentService service;
    @GetMapping
    public ApiResponse<Page<DepartmentDto>> getAll(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return new ApiResponse<>("success", "部署一覧取得成功", service.findAll(pageable));
    }
}
