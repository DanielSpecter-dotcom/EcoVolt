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
public class LimitResponseDto {

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("limit_kwh")
    private Double limitKwh;
}
