package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ConfigPresupuestoDTO {
    private BigDecimal ingresoSemanal;
    private BigDecimal ingresosMensuales;
    private ConfigDetalleDTO gastos;
    private ConfigDetalleDTO ahorros;
    private ConfigDetalleDTO inversiones;
    private ConfigDetalleDTO libre;
}