package com.ecovolt.demo.Dto.Response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResponseDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String username;
    private String correo;
}
