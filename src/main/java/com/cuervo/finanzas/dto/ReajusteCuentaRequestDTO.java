package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReajusteCuentaRequestDTO {
    private Long idCuenta;
    private BigDecimal valor; // El valor final que debe tener la cuenta
}
