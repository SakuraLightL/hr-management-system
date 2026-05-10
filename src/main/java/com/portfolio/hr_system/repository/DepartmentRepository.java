package com.portfolio.hr_system.repository;

import com.portfolio.hr_system.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

}