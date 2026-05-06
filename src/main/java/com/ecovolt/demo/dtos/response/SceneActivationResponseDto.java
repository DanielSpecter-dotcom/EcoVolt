package com.ecovolt.demo.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneActivationResponseDto {

    @JsonProperty("scene_id")
    private Long sceneId;

    @JsonProperty("activated_at")
    private LocalDateTime activatedAt;

    @JsonProperty("applied_devices")
    private List<DeviceStateResponseDto> appliedDevices;
}
