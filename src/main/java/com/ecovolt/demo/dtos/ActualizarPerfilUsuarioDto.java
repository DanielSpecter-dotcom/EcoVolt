package com.ecovolt.demo.dtos;

import com.ecovolt.demo.Enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarPerfilUsuarioDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    private String nombre;

    @Size(max = 120, message = "El apellido no debe superar 120 caracteres")
    private String apellido;

    private String correo;

    private String telefono;

    private String ciudad;

    @JsonProperty("tipo_usuario")
    private TipoUsuario tipoUsuario;
}
