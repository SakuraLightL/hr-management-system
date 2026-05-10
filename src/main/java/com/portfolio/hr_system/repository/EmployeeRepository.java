package com.portfolio.hr_system.repository;

import com.portfolio.hr_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

        Page<Employee> findByNameContaining(String name, Pageable pageable);

        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department")
        List<Employee> findAllWithDepartment();

        @Query(
                value = "SELECT e FROM Employee e LEFT JOIN FETCH e.department",
                countQuery = "SELECT COUNT(e) FROM Employee e"
        )
        Page<Employee> findAllWithDepartment(Pageable pageable);

        @Query("""
            SELECT e FROM Employee e
            LEFT JOIN FETCH e.department
            WHERE e.id = :id
            """)
        Optional<Employee> findByIdWithDepartment(@Param("id") Long id);

        @Query(
                value = """
                    SELECT e FROM Employee e
                    LEFT JOIN FETCH e.department
                    WHERE (:name IS NULL
                        OR e.name LIKE CONCAT('%', :name, '%'))
                    """,
                countQuery = """
                    SELECT COUNT(e) FROM Employee e
                    WHERE (:name IS NULL
                        OR e.name LIKE CONCAT('%', :name, '%'))
                    """
        )
        Page<Employee> searchWithDepartment(
                @Param("name") String name,
                Pageable pageable
        );
}