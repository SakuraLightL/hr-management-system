package com.portfolio.hr_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 社員名
    @Column(nullable = false, length = 100)
    private String name;

    // メール
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // 役職
    @Column(length = 100)
    private String position;

    // 所属部署
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
}