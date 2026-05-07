package com.ecovolt.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscenasRutinasPanelDto {

    private List<EscenaRutinaPanelDto> escenas;
    private List<EscenaRutinaPanelDto> rutinas;
}
