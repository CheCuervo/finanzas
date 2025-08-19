package com.cuervo.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PresupuestoDetalleDTO {
    private BigDecimal valor;
    private BigDecimal porcentaje;
}