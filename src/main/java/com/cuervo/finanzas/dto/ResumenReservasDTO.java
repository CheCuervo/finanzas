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
    private BigDecimal totalReservadoAhorros;
    private BigDecimal totalReservadoGastoFijos;
    private BigDecimal totalReservadoInversiones; // Nuevo campo
    
    private BigDecimal pptoSemanalAhorros;
    private BigDecimal pptoSemanalGastosFijos;
    private BigDecimal pptoSemanalInversiones; // Nuevo campo
    private BigDecimal pptoSemanalTotal;
    
    private BigDecimal pptoMensualAhorros;
    private BigDecimal pptoMensualGastosFijos;
    private BigDecimal pptoMensualTotal;

    private List<ReservaDetalleDTO> reservas;
}
