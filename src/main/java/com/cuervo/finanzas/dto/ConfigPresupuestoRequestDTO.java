package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ConfigPresupuestoRequestDTO {
    private BigDecimal ingresoSemanal;
    private Integer gastos;
    private Integer ahorros;
    private Integer inversiones;
    private Integer libre;
}
