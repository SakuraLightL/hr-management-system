package com.portfolio.hr_system.exception;

import com.portfolio.hr_system.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * バリデーションエラー
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error :
                ex.getBindingResult().getFieldErrors()) {

            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        log.warn(
                "Validation error occurred. errors={}, time={}",
                errors,
                LocalDateTime.now()
        );

        return new ApiResponse<>(
                "error",
                "validation error",
                errors
        );
    }

    /**
     * DB一意制約違反など
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.error(
                "Database integrity violation occurred.",
                ex
        );

        return new ApiResponse<>(
                "error",
                "database error",
                null
        );
    }

    /**
     * データ未検出
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleNotFound(
            EntityNotFoundException ex) {

        log.warn(
                "Resource not found. message={}",
                ex.getMessage()
        );

        return new ApiResponse<>(
                "error",
                "resource not found",
                null
        );
    }

    /**
     * 想定外エラー
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleException(Exception ex) {

        log.error(
                "Unexpected server error occurred.",
                ex
        );

        return new ApiResponse<>(
                "error",
                "server error",
                null
        );
    }
}