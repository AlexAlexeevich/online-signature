package com.era.onlinesignature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LoginIsExistException extends RuntimeException {
    public LoginIsExistException(String message) {
        super(message);
    }
}
