package com.portfolio.hr_system.dto;
public record ApiResponse<T>(String status, String message, T data) {}
