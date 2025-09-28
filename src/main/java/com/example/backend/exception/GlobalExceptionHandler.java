package com.example.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();

        return build(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getStatusCode().toString(), message, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();

        if (ex.getSupportedHttpMethods() != null) {
            details.put("supportedMethods", ex.getSupportedHttpMethods());
        }

        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), req, details);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();

        if (ex.getSupportedMediaTypes() != null && !ex.getSupportedMediaTypes().isEmpty()) {
            details.put("supportedMediaTypes", ex.getSupportedMediaTypes());
        }

        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", ex.getMessage(), req, details);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (ex.getSupportedMediaTypes() != null && !ex.getSupportedMediaTypes().isEmpty()) {
            details.put("acceptableMediaTypes", ex.getSupportedMediaTypes());
        }
        return build(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable", ex.getMessage(), req, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON", rootMessage(ex), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        List<Map<String, String>> fieldErrors = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> e = new LinkedHashMap<>();
            e.put("field", fe.getField());
            e.put("rejectedValue", String.valueOf(fe.getRejectedValue()));
            e.put("message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid"));
            fieldErrors.add(e);
        }

        details.put("fieldErrors", fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request body", req, details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        List<Map<String, String>> fieldErrors = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> e = new LinkedHashMap<>();
            e.put("field", fe.getField());
            e.put("rejectedValue", String.valueOf(fe.getRejectedValue()));
            e.put("message", Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid"));
            fieldErrors.add(e);
        }

        details.put("fieldErrors", fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid parameters", req, details);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,ConversionFailedException.class})
    public ResponseEntity<ErrorResponse> handleTypeMismatch(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Type Mismatch", ex.getMessage(), req, null);
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class, MissingPathVariableException.class })
    public ResponseEntity<ErrorResponse> handleMissingParameters(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, null);
    }

    @ExceptionHandler({ IllegalStateException.class, DataIntegrityViolationException.class, OptimisticLockingFailureException.class })
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req, null);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTx(TransactionSystemException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Transaction Error", rootMessage(ex), req, null);
    }
    
    @ExceptionHandler({ HttpMessageNotWritableException.class, DataAccessException.class })
    public ResponseEntity<ErrorResponse> handleServerIssues(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", rootMessage(ex), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", rootMessage(ex), req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest req, Map<String, Object> details) {
        ErrorResponse body = new ErrorResponse(Instant.now().toString(), status.value(), error, message, req.getRequestURI(), req.getMethod(), req.getHeader("X-Request-Id"), details);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(body, headers, status);
    }

    private static String rootMessage(Throwable t) {
        Throwable r = rootCause(t);
        return r.getMessage() != null ? r.getMessage() : r.toString();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null && c.getCause() != c) {
            c = c.getCause();
        }
        return c;
    }

    public static class ErrorResponse {
        public final String timestamp;
        public final int status;
        public final String error;
        public final String message;
        public final String path;
        public final String method;
        public final String requestId;
        public final Map<String, Object> details;

        public ErrorResponse(String timestamp, int status, String error, String message, String path, String method, String requestId, Map<String, Object> details) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.method = method;
            this.requestId = (requestId != null && !requestId.isBlank()) ? requestId : null;
            this.details = (details != null && !details.isEmpty()) ? details : null;
        }
    }
}