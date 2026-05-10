package com.portfolio.hr_system.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ichiji {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "admin123";
        String encoded = encoder.encode(raw);
        System.out.println(encoded);
    }
}
