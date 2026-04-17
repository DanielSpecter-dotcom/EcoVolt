package com.ecovolt.demo.Dto.Request;

import com.ecovolt.demo.Enums.TipoUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioCreateDto {
    private String dni;
    private String correo;
    private String contrasena;
    private TipoUsuario tipoUsuario;
}
