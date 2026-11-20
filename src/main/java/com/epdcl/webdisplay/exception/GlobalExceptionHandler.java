package com.epdcl.webdisplay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle AccountException
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ErrorDetails> handleAccountException(
            AccountException exception,
            WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "ACCOUNT_NOT_FOUND"
        );

        return new ResponseEntity<>(
                errorDetails,
                HttpStatus.NOT_FOUND
        );
    }

    // Handle missing resources (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDetails> handleNoResourceFoundException(
            NoResourceFoundException exception,
            WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "RESOURCE_NOT_FOUND"
        );

        return new ResponseEntity<>(
                errorDetails,
                HttpStatus.NOT_FOUND
        );
    }

    // Validation problems (bad upload, invalid theme, etc.) are the user's
    // input, not a server crash: answer 400 with the readable message.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgument(
            IllegalArgumentException exception,
            WebRequest webRequest) {

        return new ResponseEntity<>(
                new ErrorDetails(LocalDateTime.now(), exception.getMessage(),
                        webRequest.getDescription(false), "INVALID_REQUEST"),
                HttpStatus.BAD_REQUEST);
    }

    // Keep the status chosen by controllers (401, 403, ...) instead of turning it into 500.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDetails> handleResponseStatus(
            ResponseStatusException exception,
            WebRequest webRequest) {

        return new ResponseEntity<>(
                new ErrorDetails(LocalDateTime.now(), exception.getReason(),
                        webRequest.getDescription(false), "REQUEST_REJECTED"),
                exception.getStatusCode());
    }

    // Handle generic exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(
            Exception exception,
            WebRequest webRequest) {

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false),
                "INTERNAL_SERVER_ERROR"
        );

        return new ResponseEntity<>(
                errorDetails,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}