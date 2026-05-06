package com.ecovolt.demo.services.feingservice;

import com.ecovolt.demo.dtos.response.ReniecResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ReniecClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.decolecta.com/v1/reniec/dni")
            .build();

    public ReniecResponse getPersonaInfo(String numero, String token) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("numero", numero)
                        .build())
                .header("Authorization", token)
                .retrieve()
                .body(ReniecResponse.class);
    }
}