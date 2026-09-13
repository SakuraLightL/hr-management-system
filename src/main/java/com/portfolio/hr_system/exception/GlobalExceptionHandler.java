package com.portfolio.hr_system.exception;
import com.portfolio.hr_system.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.*;

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BindException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String,String>> validation(BindException ex) {
        Map<String,String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ApiResponse<>("error", "入力内容を確認してください", errors);
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> badRequest(Exception ex) {
        String message = ex instanceof IllegalArgumentException && !(ex instanceof MethodArgumentTypeMismatchException)
                ? ex.getMessage() : "入力形式が正しくありません";
        return new ApiResponse<>("error", message, null);
    }
    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class}) @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> notFound(Exception ex) { return new ApiResponse<>("error", "データが見つかりません", null); }
    @ExceptionHandler(DuplicateResourceException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> duplicate(DuplicateResourceException ex) { return new ApiResponse<>("error", ex.getMessage(), null); }
    @ExceptionHandler(DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> integrity(DataIntegrityViolationException ex) {
        // Constraint errors can contain entire rows (PII), so do not log the SQL exception.
        log.warn("Data integrity conflict");
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
        String message = detail.contains("uk_employees_email") || detail.contains("uk_users_email")
                ? "このメールは登録済みです（削除済みデータを含みます）"
                : detail.contains("uk_users_username") ? "このユーザー名は登録済みです"
                : "データが重複しているか、他のデータから参照されています";
        return new ApiResponse<>("error", message, null);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> method(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(ex.getHeaders())
                .body(new ApiResponse<>("error", "このHTTPメソッドは使用できません", null));
    }
    @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> unexpected(Exception ex) {
        log.error("Unexpected error type={}", ex.getClass().getSimpleName());
        return new ApiResponse<>("error", "サーバーエラーが発生しました", null);
    }
}
