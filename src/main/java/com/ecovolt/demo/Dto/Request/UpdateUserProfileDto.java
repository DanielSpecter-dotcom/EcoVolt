package com.ecovolt.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileDto {

    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    private String nombre;

    @JsonProperty("foto_perfil_url")
    @Size(max = 500, message = "La URL de la foto no debe superar 500 caracteres")
    private String fotoPerfilUrl;
}
