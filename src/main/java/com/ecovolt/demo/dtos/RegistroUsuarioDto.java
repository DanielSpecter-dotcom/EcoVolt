package com.ecovolt.demo.dtos;

import com.ecovolt.demo.Enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = false)
public class RegistroUsuarioDto {

    private final Map<String, Object> camposNoPermitidos = new HashMap<>();

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 digitos numericos")
    private String dni;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    @JsonProperty("contrasena")
    private String contrasena;

    @NotNull(message = "El tipo de uso es obligatorio")
    @JsonProperty("tipo_uso")
    private TipoUsuario tipoUso;

    @JsonProperty("nombre_empresa")
    @Size(max = 150, message = "El nombre de empresa no debe superar 150 caracteres")
    private String nombreEmpresa;

    @Size(min = 11, max = 11, message = "El RUC debe tener 11 caracteres")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe contener solo digitos")
    private String ruc;

    @JsonAnySetter
    public void setCampoNoPermitido(String nombreCampo, Object valor) {
        camposNoPermitidos.put(nombreCampo, valor);
    }

    @AssertTrue(message = "Solo se permiten los campos dni, correo, contrasena, tipo_uso, nombre_empresa y ruc")
    public boolean isSinCamposNoPermitidos() {
        return camposNoPermitidos.isEmpty();
    }

    @AssertTrue(message = "Para tipo_uso PERSONAL no debe enviar nombre_empresa ni ruc")
    public boolean isCamposPersonalValidos() {
        if (tipoUso != TipoUsuario.PERSONAL) {
            return true;
        }
        return isBlank(nombreEmpresa) && isBlank(ruc);
    }

    @AssertTrue(message = "Para tipo_uso EMPRESARIAL debe enviar nombre_empresa y ruc")
    public boolean isCamposEmpresarialValidos() {
        if (tipoUso != TipoUsuario.EMPRESARIAL) {
            return true;
        }
        return !isBlank(nombreEmpresa) && !isBlank(ruc);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
