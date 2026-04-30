package com.ecovolt.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VerificationSentResponseDto {

    private String correo;

    @JsonProperty("verification_token")
    private String verificationToken;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("verification_link")
    private String verificationLink;
}
