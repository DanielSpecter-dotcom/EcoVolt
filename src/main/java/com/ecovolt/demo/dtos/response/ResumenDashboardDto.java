package com.ecovolt.demo.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDashboardDto {

    private Double consumoDiarioKwh;
    private Double consumoMensualKwh;
    private Double costoEstimadoSoles;
    private Double variacionPorcentaje;
}
