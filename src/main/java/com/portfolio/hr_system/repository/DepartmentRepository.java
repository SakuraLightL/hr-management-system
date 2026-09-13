package com.portfolio.hr_system.repository;

import com.portfolio.hr_system.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    /**
     * 部署名検索
     */
    Optional<Department> findByName(String name);

    /**
     * 部署名部分一致検索
     */
    List<Department> findByNameContaining(String name);

    /**
     * 部署名昇順取得
     */
    List<Department> findAllByOrderByNameAsc();
}