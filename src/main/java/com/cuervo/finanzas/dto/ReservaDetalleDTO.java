package com.cuervo.finanzas.dto;

import com.cuervo.finanzas.entity.enums.TipoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDetalleDTO {
    private Long id;
    private String concepto;
    private TipoReserva tipo;
    private BigDecimal valorMeta;
    private BigDecimal valorReservaSemanal;
    private LocalDate fechaMeta;
    private BigDecimal valorAhorrado;
    private BigDecimal valorGastado;
    private BigDecimal valorReservado;
    private BigDecimal valorFaltante;
}
