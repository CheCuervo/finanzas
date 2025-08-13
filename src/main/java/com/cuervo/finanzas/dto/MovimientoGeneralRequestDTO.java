package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoGeneralRequestDTO {
    private Long idCuenta;
    private BigDecimal valor;
    private String concepto;
    private String tipoMovimiento; // Debe ser "INGRESO" o "EGRESO"
}
