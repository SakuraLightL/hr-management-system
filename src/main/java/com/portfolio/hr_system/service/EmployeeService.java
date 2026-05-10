package com.portfolio.hr_system.service;

import com.portfolio.hr_system.dto.EmployeeDto;
import com.portfolio.hr_system.entity.Department;
import com.portfolio.hr_system.entity.Employee;
import com.portfolio.hr_system.repository.DepartmentRepository;
import com.portfolio.hr_system.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public EmployeeDto findDtoById(Long id) {
        log.debug("社員DTO取得開始 employeeId={}", id);

        Employee employee = employeeRepository.findByIdWithDepartment(id)
                .orElse(null);

        if (employee == null) {
            log.warn("社員が見つかりません employeeId={}", id);
            return null;
        }

        return convertToDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> findAllDto() {
        log.debug("社員一覧取得開始");

        List<EmployeeDto> employees = employeeRepository.findAllWithDepartment()
                .stream()
                .map(this::convertToDto)
                .toList();

        log.debug("社員一覧取得完了 count={}", employees.size());

        return employees;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> findPage(Pageable pageable) {
        log.debug("社員ページ取得開始 page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<EmployeeDto> page = employeeRepository.findAllWithDepartment(pageable)
                .map(this::convertToDto);

        log.debug("社員ページ取得完了 totalElements={}, totalPages={}",
                page.getTotalElements(), page.getTotalPages());

        return page;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> searchPage(String name, Pageable pageable) {
        String keyword = normalizeKeyword(name);

        log.debug("社員ページ検索開始 keyword={}, page={}, size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());

        Page<EmployeeDto> dtoPage = employeeRepository.searchWithDepartment(keyword, pageable)
                .map(this::convertToDto);

        log.debug("社員ページ検索完了 keyword={}, totalElements={}, totalPages={}",
                keyword, dtoPage.getTotalElements(), dtoPage.getTotalPages());

        return dtoPage;
    }

    public Employee saveFromDto(EmployeeDto dto) {
        log.info("社員登録/更新開始 employeeId={}, departmentId={}",
                dto.getId(), dto.getDepartmentId());

        Employee employee = resolveEmployee(dto.getId());

        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setPosition(dto.getPosition());
        employee.setDepartment(resolveDepartment(dto.getDepartmentId()));

        Employee saved = employeeRepository.save(employee);

        log.info("社員登録/更新完了 employeeId={}", saved.getId());

        return saved;
    }

    public void delete(Long id) {
        log.info("社員削除開始 employeeId={}", id);

        if (!employeeRepository.existsById(id)) {
            log.warn("削除対象の社員が存在しません employeeId={}", id);
            throw new IllegalArgumentException("社員が見つかりません employeeId=" + id);
        }

        employeeRepository.deleteById(id);

        log.info("社員削除完了 employeeId={}", id);
    }

    private Employee resolveEmployee(Long id) {
        if (id == null) {
            return new Employee();
        }

        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "社員が見つかりません employeeId=" + id
                ));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "部署が見つかりません departmentId=" + departmentId
                ));
    }

    private String normalizeKeyword(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }

    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();

        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition());

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        return dto;
    }
}