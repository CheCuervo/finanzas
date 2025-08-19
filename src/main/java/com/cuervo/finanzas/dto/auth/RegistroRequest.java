package com.cuervo.finanzas.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistroRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private BigDecimal ingresoSemanal; // Nuevo campo obligatorio
}
