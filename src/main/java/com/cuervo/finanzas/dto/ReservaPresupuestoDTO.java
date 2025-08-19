package com.cuervo.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaPresupuestoDTO {
    private Long id;
    private String concepto;
    private BigDecimal valorReservaSemanal;
}