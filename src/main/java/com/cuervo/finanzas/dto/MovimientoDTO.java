package com.cuervo.finanzas.dto;

import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.cuervo.finanzas.entity.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoDTO {
    private Long id;
    private LocalDateTime fecha;
    private String concepto;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal valor;
    private String cuentaDescripcion; // Nuevo campo
    private TipoCuenta cuentaTipo;      // Nuevo campo
}
