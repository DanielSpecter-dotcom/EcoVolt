package com.ecovolt.demo.dtos;

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
}
