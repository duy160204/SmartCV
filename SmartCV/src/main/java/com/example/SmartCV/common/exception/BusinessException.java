package com.example.SmartCV.common.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String messageKey;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.messageKey = null;
    }

    public BusinessException(String message, String messageKey) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.messageKey = messageKey;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.messageKey = null;
    }

    public BusinessException(String message, String messageKey, HttpStatus status) {
        super(message);
        this.status = status;
        this.messageKey = messageKey;
    }
}
