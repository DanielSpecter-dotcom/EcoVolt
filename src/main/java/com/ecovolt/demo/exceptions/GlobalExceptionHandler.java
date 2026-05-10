package com.ecovolt.demo.exceptions;

import com.ecovolt.demo.dtos.RespuestaApi;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RespuestaApi<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new RespuestaApi<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RespuestaApi<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespuestaApi<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespuestaApi<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new RespuestaApi<>(false, "Credenciales invalidas", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespuestaApi<>(false, message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaApi<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof UnrecognizedPropertyException propertyException) {
            String message = "Campo no permitido: " + propertyException.getPropertyName();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RespuestaApi<>(false, message, null));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespuestaApi<>(false, "El cuerpo de la solicitud no tiene un formato valido", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RespuestaApi<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespuestaApi<>(false, ex.getMessage(), null));
    }
}
