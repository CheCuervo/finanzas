package com.cuervo.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenReservasDTO {
    private BigDecimal totalReservado;
    private BigDecimal totalReservadoAhorros; // Renombrado
    private BigDecimal totalReservadoGastoFijos; // Renombrado
    private List<ReservaDetalleDTO> reservas;
}
