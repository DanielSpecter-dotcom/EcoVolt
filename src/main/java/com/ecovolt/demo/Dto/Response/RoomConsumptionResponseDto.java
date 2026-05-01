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
public class RoomConsumptionResponseDto {

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("total_kwh")
    private Double totalKwh;

    @JsonProperty("total_duration_minutes")
    private Integer totalDurationMinutes;

    private List<ConsumptionCompareItemDto> devices;
}
