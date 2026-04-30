package com.ecovolt.demo.Dto.Response;


import com.ecovolt.demo.Enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsuarioResponseDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String username;
    private String correo;

    @JsonProperty("tipo_usuario")
    private TipoUsuario tipoUsuario;

    private boolean activo;

    @JsonProperty("consumo_excesivo")
    private boolean notificarConsumoExcesivo;

    @JsonProperty("uso_prolongado")
    private boolean notificarUsoProlongado;

    @JsonProperty("reporte_semanal")
    private boolean notificarReporteSemanal;

    private List<String> roles;
}
