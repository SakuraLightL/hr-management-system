package com.portfolio.hr_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 部署名
    @Column(nullable = false, length = 100)
    private String name;

    // 部署説明
    @Column(length = 500)
    private String description;
}