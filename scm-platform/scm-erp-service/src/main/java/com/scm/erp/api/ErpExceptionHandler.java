package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class ErpExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> illegalState(IllegalStateException ex) {
        String code = ex.getMessage() != null && ex.getMessage().startsWith("ERP_")
                ? ex.getMessage() : "ERP_09001";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code, code, UUID.randomUUID().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> illegalArg(IllegalArgumentException ex) {
        String code = ex.getMessage() != null && ex.getMessage().startsWith("ERP_")
                ? ex.getMessage() : "ERP_02002";
        HttpStatus status = "ERP_10002".equals(code) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code, code, UUID.randomUUID().toString()));
    }
}
