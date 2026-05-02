package com.ecovolt.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionResponseDto {

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("daily_kwh")
    private Double dailyKwh;

    @JsonProperty("weekly_kwh")
    private Double weeklyKwh;

    @JsonProperty("monthly_kwh")
    private Double monthlyKwh;
}
