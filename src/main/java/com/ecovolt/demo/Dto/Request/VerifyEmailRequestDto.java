package com.ecovolt.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequestDto {

    @NotBlank(message = "El token es obligatorio")
    private String token;
}
