package com.flowops.shared.infrastructure;

import com.flowops.shared.domain.BusinessRuleException;
import com.flowops.shared.domain.ConcurrencyConflictException;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.shared.domain.ForbiddenActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponseDto.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(ConcurrencyConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConcurrencyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponseDto.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponseDto> handleForbidden(ForbiddenActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponseDto.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDto> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponseDto.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponseDto.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> ErrorResponseDto.ErrorDetail.builder()
                        .field(err.getField())
                        .reason(err.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponseDto.builder()
                        .code("INVALID_INPUT")
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .details(details)
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponseDto.builder()
                        .code("MISSING_HEADER")
                        .message("Thiếu header bắt buộc: " + ex.getHeaderName())
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponseDto.builder()
                        .code("ACCESS_DENIED")
                        .message("Bạn không có quyền thực hiện hành động này")
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDto.builder()
                        .code("UNAUTHORIZED")
                        .message("Xác thực không thành công hoặc phiên đã hết hạn")
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponseDto.builder()
                        .code("INTERNAL_ERROR")
                        .message("Đã xảy ra sự cố nội bộ trên máy chủ")
                        .traceId(UUID.randomUUID().toString())
                        .build()
        );
    }
}
