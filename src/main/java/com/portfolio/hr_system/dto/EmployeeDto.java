package com.portfolio.hr_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;

@lombok.Getter @lombok.Setter
public class EmployeeDto {
    @jakarta.validation.constraints.NotNull
    private com.portfolio.hr_system.entity.EmploymentStatus employmentStatus = com.portfolio.hr_system.entity.EmploymentStatus.ACTIVE;
    @jakarta.validation.constraints.NotNull
    private com.portfolio.hr_system.entity.EmploymentType employmentType = com.portfolio.hr_system.entity.EmploymentType.PERMANENT;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate hireDate;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate retirementDate;


    @Positive
    private Long id;

    @NotBlank(message = "名前は必須です")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "メールは必須です")
    @Size(max = 255)
    @Email(message = "メール形式で入力してください")
    private String email;

    @NotBlank(message = "役職は必須です")
    @Size(max = 100)
    private String position;

    // Department連携用
    @Positive
    private Long departmentId;

    // 画面表示用
    private String departmentName;

    public EmployeeDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}