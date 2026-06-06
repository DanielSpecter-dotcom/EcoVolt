package com.ecovolt.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificarCorreoDto {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    private String correo;

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El codigo debe tener exactamente 6 digitos numericos")
    private String codigo;
}
