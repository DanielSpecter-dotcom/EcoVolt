package com.ecovolt.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.InicioSesionSolicitudDto;
import com.ecovolt.demo.dtos.request.RegistroUsuarioDto;
import com.ecovolt.demo.dtos.request.ReenviarVerificacionDto;
import com.ecovolt.demo.dtos.request.VerificarCorreoDto;
import com.ecovolt.demo.dtos.response.RespuestaApi;
import com.ecovolt.demo.dtos.response.InicioSesionRespuestaDto;
import com.ecovolt.demo.dtos.response.VerificacionEnviadaRespuestaDto;
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
    public ResponseEntity<RespuestaApi<InicioSesionRespuestaDto>> login(@Valid @RequestBody InicioSesionSolicitudDto request) {
        InicioSesionRespuestaDto data = autenticacionService.login(request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Autenticacion exitosa", data));
    }

    @PostMapping("/register")
    public ResponseEntity<RespuestaApi<VerificacionEnviadaRespuestaDto>> register(
            @Valid @RequestBody RegistroUsuarioDto request) {
        VerificacionEnviadaRespuestaDto data = autenticacionService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaApi<>(true, "Usuario registrado. Verifique su correo para activar la cuenta", data));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<RespuestaApi<Void>> verifyEmail(@Valid @RequestBody VerificarCorreoDto request) {
        autenticacionService.verifyEmail(request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Cuenta activada exitosamente", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<RespuestaApi<VerificacionEnviadaRespuestaDto>> resendVerification(
            @Valid @RequestBody ReenviarVerificacionDto request) {
        VerificacionEnviadaRespuestaDto data = autenticacionService.resendVerification(request);
        return ResponseEntity.ok(new RespuestaApi<>(true, "Correo de verificacion reenviado", data));
    }
}
