package com.cuervo.finanzas.dto;

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
public class MovimientoReservaDTO {
    private Long id;
    private LocalDateTime fecha;
    private String concepto;
    private String tipoMovimiento; // "Reserva" o "Pago"
    private BigDecimal valor;
    private String cuentaDescripcion; // La cuenta de origen (si aplica)
}
