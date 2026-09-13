package com.portfolio.hr_system.service;
import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.entity.*;
import com.portfolio.hr_system.exception.DuplicateResourceException;
import com.portfolio.hr_system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional @RequiredArgsConstructor @Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    @Transactional(readOnly = true)
    public EmployeeDto findDtoById(Long id) { return toDto(require(id)); }
    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchPage(String name, Pageable pageable) {
        Page<Employee> page = name == null || name.isBlank() ? employeeRepository.findAll(pageable)
                : employeeRepository.findByNameStartingWith(name.trim(), pageable);
        return page.map(this::toDto);
    }
    public EmployeeDto saveFromDto(EmployeeDto dto) {
        Employee employee = dto.getId() == null ? new Employee() : require(dto.getId());
        String email = dto.getEmail().trim();
        boolean duplicate = dto.getId() == null ? employeeRepository.existsByEmail(email)
                : employeeRepository.existsByEmailAndIdNot(email, dto.getId());
        if (duplicate) throw new DuplicateResourceException("このメールは登録済みです");
        if (dto.getEmploymentStatus() == EmploymentStatus.RETIRED && dto.getRetirementDate() == null)
            throw new IllegalArgumentException("退職の場合は退職日が必要です");
        if (dto.getEmploymentStatus() != EmploymentStatus.RETIRED && dto.getRetirementDate() != null)
            throw new IllegalArgumentException("退職日は退職ステータスの場合のみ設定できます");
        if (dto.getHireDate() != null && dto.getRetirementDate() != null
                && dto.getRetirementDate().isBefore(dto.getHireDate()))
            throw new IllegalArgumentException("退職日は入社日以降にしてください");
        employee.setName(dto.getName().trim()); employee.setEmail(email); employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartmentId() == null ? null : departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("指定した部署が見つかりません")));
        employee.setEmploymentStatus(dto.getEmploymentStatus()); employee.setEmploymentType(dto.getEmploymentType());
        employee.setHireDate(dto.getHireDate()); employee.setRetirementDate(dto.getRetirementDate());
        Employee saved = employeeRepository.save(employee);
        log.info("Employee saved id={}", saved.getId());
        return toDto(saved);
    }
    public void delete(Long id) {
        Employee employee = require(id);
        employee.setDeleted(true);
        employeeRepository.save(employee); // Normal update keeps auditing active for soft deletion.
        log.info("Employee archived id={}", id);
    }
    private Employee require(Long id) {
        return employeeRepository.findById(id).filter(employee -> !employee.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません"));
    }
    private EmployeeDto toDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId()); dto.setName(employee.getName()); dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition()); dto.setEmploymentStatus(employee.getEmploymentStatus());
        dto.setEmploymentType(employee.getEmploymentType()); dto.setHireDate(employee.getHireDate());
        dto.setRetirementDate(employee.getRetirementDate());
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId()); dto.setDepartmentName(employee.getDepartment().getName());
        }
        return dto;
    }
}
