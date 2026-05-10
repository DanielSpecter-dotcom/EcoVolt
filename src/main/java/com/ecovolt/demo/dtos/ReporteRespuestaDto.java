package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRespuestaDto {

    @JsonProperty("total_kwh")
    private Double totalKwh;

    @JsonProperty("total_duration_minutes")
    private Integer totalDurationMinutes;

    @JsonProperty("device_count")
    private Integer deviceCount;

    @JsonProperty("alert_count")
    private Integer alertCount;

    @JsonProperty("top_consumers")
    private List<ItemComparacionConsumoDto> topConsumers;
}
