package com.ecovolt.demo.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscenaRutinaPanelDto {

    private Long id;
    private String nombre;
    private String tipo;
    private String estado;
}
