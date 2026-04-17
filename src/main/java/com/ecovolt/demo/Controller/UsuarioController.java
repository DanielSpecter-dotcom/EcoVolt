package com.ecovolt.demo.Controller;

import com.ecovolt.demo.Dto.Request.UsuarioCreateDto;
import com.ecovolt.demo.Dto.Response.ApiResponse;
import com.ecovolt.demo.Dto.Response.UsuarioResponseDto;
import com.ecovolt.demo.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/EcoVolt/v1/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> saveUsuario(@Valid @RequestBody UsuarioCreateDto usuarioCreateDto) {
        UsuarioResponseDto data = usuarioService.saveUsuario(usuarioCreateDto);
        return new ResponseEntity<>
                (new ApiResponse<>(true,"Usuario creado exitosamente", data), HttpStatus.CREATED);
    }
}
