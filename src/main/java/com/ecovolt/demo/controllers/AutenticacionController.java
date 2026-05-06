package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.LoginRequestDto;
import com.ecovolt.demo.dtos.request.RegisterRequestDto;
import com.ecovolt.demo.dtos.request.ResendVerificationRequestDto;
import com.ecovolt.demo.dtos.request.VerifyEmailRequestDto;
import com.ecovolt.demo.dtos.response.ApiResponse;
import com.ecovolt.demo.dtos.response.LoginResponseDto;
import com.ecovolt.demo.dtos.response.VerificationSentResponseDto;
import com.ecovolt.demo.serviceimpl.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionController {

    @Autowired
    private AutenticacionService autenticacionService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto data = autenticacionService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Autenticacion exitosa", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<VerificationSentResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        VerificationSentResponseDto data = autenticacionService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Usuario registrado. Verifique su correo para activar la cuenta", data));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        autenticacionService.verifyEmail(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cuenta activada exitosamente", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<VerificationSentResponseDto>> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDto request) {
        VerificationSentResponseDto data = autenticacionService.resendVerification(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Correo de verificacion reenviado", data));
    }
}
