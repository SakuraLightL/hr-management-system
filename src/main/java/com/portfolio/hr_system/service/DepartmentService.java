package com.portfolio.hr_system.service;
import com.portfolio.hr_system.dto.DepartmentDto;
import com.portfolio.hr_system.entity.Department;
import com.portfolio.hr_system.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional @RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;
    @Transactional(readOnly = true)
    public Page<DepartmentDto> findAll(Pageable pageable) { return repository.findAll(pageable).map(this::toDto); }
    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) { return toDto(require(id)); }
    private Department require(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("部署が見つかりません"));
    }
    public void save(DepartmentDto dto) {
        Department department = dto.getId() == null ? new Department() : require(dto.getId());
        department.setName(dto.getName().trim()); department.setDescription(dto.getDescription());
        repository.save(department);
    }
    public void delete(Long id) { repository.delete(require(id)); }
    private DepartmentDto toDto(Department department) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId()); dto.setName(department.getName()); dto.setDescription(department.getDescription());
        return dto;
    }
}
