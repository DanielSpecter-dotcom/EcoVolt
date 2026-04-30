package com.ecovolt.demo.Dto.Response;


import com.ecovolt.demo.Enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("tipo_usuario")
    private TipoUsuario tipoUsuario;

    @JsonProperty("foto_perfil_url")
    private String fotoPerfilUrl;

    private boolean activo;

    @JsonProperty("consumo_excesivo")
    private boolean notificarConsumoExcesivo;

    @JsonProperty("uso_prolongado")
    private boolean notificarUsoProlongado;

    @JsonProperty("reporte_semanal")
    private boolean notificarReporteSemanal;
}
