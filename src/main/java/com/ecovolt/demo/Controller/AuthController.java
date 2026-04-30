package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.RegisterRequestDto;
import com.ecovolt.demo.Dto.Request.ResendVerificationRequestDto;
import com.ecovolt.demo.Dto.Request.VerifyEmailRequestDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.VerificationSentResponseDto;
import com.ecovolt.demo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<VerificationSentResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        VerificationSentResponseDto data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Usuario registrado. Verifique su correo para activar la cuenta", data));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cuenta activada exitosamente", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<VerificationSentResponseDto>> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDto request) {
        VerificationSentResponseDto data = authService.resendVerification(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Correo de verificacion reenviado", data));
    }
}
