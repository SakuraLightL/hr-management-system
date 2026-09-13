package com.portfolio.hr_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ログイン名
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    // パスワード
    @Column(length = 255)
    private String password;

    // 権限
    // ADMIN / USER
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private Role role = Role.USER;

    // Googleログイン用メール
    @Column(unique = true, length = 255)
    private String email;

    // Google Provider ID
    @Column(unique = true, length = 255)
    private String providerId;

    // LOCAL / GOOGLE
    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    private AuthProvider provider = AuthProvider.LOCAL;
}
