package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
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
public class HabitacionDTO {

    private Long id;
    private String name;

    @JsonProperty("casa_id")
    @JsonAlias("home_id")
    private Long casaId;
}
