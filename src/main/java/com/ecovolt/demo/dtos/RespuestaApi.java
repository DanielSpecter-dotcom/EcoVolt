package com.ecovolt.demo.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespuestaApi<T> { // Body General
    private boolean success; // true o false
    private String message; // resumen de los errores si es que existen
    private T data; // null si es que no hay informacion que retornar
//    private List<String> errors;
}
