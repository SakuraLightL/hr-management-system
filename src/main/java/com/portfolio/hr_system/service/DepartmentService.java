package com.portfolio.hr_system.service;

import com.portfolio.hr_system.entity.Department;
import com.portfolio.hr_system.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Department findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Department save(Department department) {
        return repository.save(department);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}