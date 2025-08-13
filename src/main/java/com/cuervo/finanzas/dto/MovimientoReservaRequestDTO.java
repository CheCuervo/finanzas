package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoReservaRequestDTO {
    private String tipoMovimiento; // "Reserva" o "Pago"
    private Long idCuenta;
    private BigDecimal valor;
    private Long idReserva; // Campo renombrado
    private String concepto;
}
