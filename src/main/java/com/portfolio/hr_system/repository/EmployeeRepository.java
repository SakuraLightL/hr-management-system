package com.portfolio.hr_system.repository;

import com.portfolio.hr_system.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

        /**
         * 名前部分一致検索
         */
        Page<Employee> findByNameContaining(
                String name,
                Pageable pageable
        );

        /**
         * 部署込み全件取得
         */
        @EntityGraph(attributePaths = "department")
        @Query("""
           SELECT e FROM Employee e
           """)
        List<Employee> findAllWithDepartment();

        /**
         * 部署込みページ取得
         */
        @EntityGraph(attributePaths = "department")
        @Query(
                value = """
                    SELECT e FROM Employee e
                    """,
                countQuery = """
                    SELECT COUNT(e) FROM Employee e
                    """
        )
        Page<Employee> findAllWithDepartment(
                Pageable pageable
        );

        /**
         * ID検索（部署込み）
         */
        @EntityGraph(attributePaths = "department")
        Optional<Employee> findById(Long id);

        /**
         * 名前検索（部署込み）
         */
        @EntityGraph(attributePaths = "department")
        @Query(
                value = """
                    SELECT e FROM Employee e
                    WHERE (
                        :name IS NULL
                        OR :name = ''
                        OR e.name LIKE CONCAT('%', :name, '%')
                    )
                    """,
                countQuery = """
                    SELECT COUNT(e) FROM Employee e
                    WHERE (
                        :name IS NULL
                        OR :name = ''
                        OR e.name LIKE CONCAT('%', :name, '%')
                    )
                    """
        )
        Page<Employee> searchWithDepartment(
                @Param("name") String name,
                Pageable pageable
        );
}