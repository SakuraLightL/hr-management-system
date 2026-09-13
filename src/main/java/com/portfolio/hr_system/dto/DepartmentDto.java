package com.portfolio.hr_system.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DepartmentDto {
    @Positive private Long id;
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 500) private String description;
}
