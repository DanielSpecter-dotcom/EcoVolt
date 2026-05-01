package com.ecovolt.demo.Dto.Response;

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
public class ConsumptionCompareResponseDto {

    @JsonProperty("total_kwh")
    private Double totalKwh;

    private List<ConsumptionCompareItemDto> devices;
}
