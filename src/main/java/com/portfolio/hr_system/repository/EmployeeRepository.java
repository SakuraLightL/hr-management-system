package com.portfolio.hr_system.repository;
import com.portfolio.hr_system.entity.Employee;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Override @EntityGraph(attributePaths = "department")
    Page<Employee> findAll(Pageable pageable);
    @Override @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(Long id);
    @EntityGraph(attributePaths = "department")
    Page<Employee> findByNameStartingWith(String name, Pageable pageable);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByEmail(String email);
}
