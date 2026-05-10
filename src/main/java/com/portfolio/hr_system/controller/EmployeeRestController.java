package com.portfolio.hr_system.controller;

import com.portfolio.hr_system.dto.ApiResponse;
import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.entity.Employee;
import com.portfolio.hr_system.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Employee API", description = "社員情報管理API")
@RestController
@RequestMapping("/api/employees")
public class EmployeeRestController {

    private final EmployeeService service;

    public EmployeeRestController(EmployeeService service) {
        this.service = service;
    }

    @Operation(
            summary = "社員一覧取得",
            description = "検索条件・ページング条件付きで社員一覧を取得します"
    )
    @GetMapping
    public ApiResponse<Page<EmployeeDto>> getEmployees(
            @RequestParam(required = false) String name,
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<EmployeeDto> page = service.searchPage(name, pageable);

        return new ApiResponse<>(
                "success",
                "社員一覧を取得しました",
                page
        );
    }

    @Operation(summary = "社員詳細取得", description = "IDを指定して社員情報を取得します")
    @GetMapping("/{id:[0-9]+}")
    public ApiResponse<EmployeeDto> getById(@PathVariable Long id) {

        EmployeeDto dto = service.findDtoById(id);

        if (dto == null) {
            return new ApiResponse<>(
                    "error",
                    "社員が見つかりません",
                    null
            );
        }

        return new ApiResponse<>(
                "success",
                "社員情報を取得しました",
                dto
        );
    }

    @Operation(summary = "社員登録", description = "社員情報を新規登録します")
    @PostMapping
    public ApiResponse<Employee> create(
            @Valid @RequestBody EmployeeDto dto) {

        Employee saved = service.saveFromDto(dto);

        return new ApiResponse<>(
                "success",
                "登録成功",
                saved
        );
    }

    @Operation(summary = "社員更新", description = "IDを指定して社員情報を更新します")
    @PutMapping("/{id:[0-9]+}")
    public ApiResponse<Employee> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto dto) {

        dto.setId(id);

        Employee updated = service.saveFromDto(dto);

        return new ApiResponse<>(
                "success",
                "更新成功",
                updated
        );
    }

    @Operation(summary = "社員削除", description = "IDを指定して社員情報を削除します")
    @DeleteMapping("/{id:[0-9]+}")
    public ApiResponse<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return new ApiResponse<>(
                "success",
                "削除成功",
                null
        );
    }
    @GetMapping("/search")
    public ApiResponse<Page<EmployeeDto>> search(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Page<EmployeeDto> page = service.searchPage(name, pageable);

        return new ApiResponse<>(
                "success",
                "社員検索結果を取得しました",
                page
        );
    }
}