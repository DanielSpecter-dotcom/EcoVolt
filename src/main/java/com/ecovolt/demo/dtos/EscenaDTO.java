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
public class EscenaDTO {

    private Long id;
    private String name;

    @JsonProperty("devices")
    private List<EstadoDeseadoDispositivoDto> devices;
}
