package com.ecovolt.demo.dtos.response;

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
public class ItemComparacionConsumoDto {

    @JsonProperty("device_id")
    private Long deviceId;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("total_kwh")
    private Double totalKwh;

    @JsonProperty("percentage")
    private Double percentage;
}
