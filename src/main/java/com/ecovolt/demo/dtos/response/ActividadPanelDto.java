package com.ecovolt.demo.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPanelDto {

    private LocalDateTime hora;
    private String descripcion;
    private String tipo;
}
