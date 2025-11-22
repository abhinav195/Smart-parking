package com.example.smartparking.api;

import com.example.smartparking.domain.BusinessRuleException;
import com.example.smartparking.domain.ConflictException;
import com.example.smartparking.domain.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final URI typeBase;

    public GlobalExceptionHandler(
            @Value("${app.errors.type-base:https://example.com/errors}") String typeBase) {
        this.typeBase = URI.create(typeBase);
    }

    // Bean Validation: @Valid @RequestBody

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(type("validation"));
        decorate(problemDetail, "validation_failed");

        List<Map<String, Object>> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("field", fe.getField());
            map.put("message", fe.getDefaultMessage());
            map.put("rejectedValue", fe.getRejectedValue());
            fieldErrors.add(map);
        }
        problemDetail.setProperty("fieldErrors", fieldErrors);

        return new ResponseEntity<>(problemDetail, headers, status);
    }

    // Bean Validation: @RequestParam / @PathVariable etc.

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail onConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint violations");
        pd.setTitle("Validation Failed");
        pd.setType(type("validation"));
        decorate(pd, "constraint_violation");

        List<Map<String, Object>> violations = ex.getConstraintViolations()
                .stream()
                .map(this::toViolationMap)
                .toList();

        pd.setProperty("violations", violations);
        return pd;
    }

    private Map<String, Object> toViolationMap(ConstraintViolation<?> v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("property", v.getPropertyPath().toString());
        map.put("message", v.getMessage());
        map.put("invalidValue", v.getInvalidValue());
        return map;
    }

    // Domain exceptions

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail onNotFoundException(NotFoundException ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setType(type("not-found"));
        decorate(pd, "not_found");
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail onConflictException(ConflictException ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        pd.setType(type("conflict"));
        decorate(pd, "conflict");
        return pd;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail onBusinessRuleException(BusinessRuleException ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Business Rule Violation");
        pd.setType(type("business-rule"));
        String code = ex.errorCode() != null ? ex.errorCode() : "business_rule_violation";
        decorate(pd, code);
        return pd;
    }

    // Type mismatches / bad requests

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail onMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameter");
        pd.setTitle("Bad Request");
        pd.setType(type("bad-request"));
        decorate(pd, "bad_request");
        pd.setProperty("parameter", ex.getName());
        pd.setProperty("requiredType",
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null);
        pd.setProperty("value", ex.getValue());
        return pd;
    }

    // Last-resort fallback

    @ExceptionHandler(Throwable.class)
    public ProblemDetail onUnhandledException(Throwable ex) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        pd.setTitle("Internal Server Error");
        pd.setType(type("internal-server-error"));
        decorate(pd, "internal_server_error");
        return pd;
    }

    // Helpers

    private void decorate(ProblemDetail problemDetail, String errorCode) {
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("correlationId", correlationId());
    }

    private URI type(String last) {
        String base = this.typeBase.toString();
        return URI.create(base.endsWith("/") ? base + last : base + "/" + last);
    }

    private String correlationId() {
        String cid = MDC.get("correlationId");
        if (cid == null) {
            cid = MDC.get("traceId");
        }
        return cid;
    }
}