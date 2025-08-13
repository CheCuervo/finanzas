package com.cuervo.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenFinancieroDTO {
    private BigDecimal dineroTotal;
    private BigDecimal dineroDisponible;
    private BigDecimal dineroReservado;
    private BigDecimal totalProyecciones; // Nuevo campo
    private BigDecimal dineroTotalConProyecciones; // Nuevo campo
    private BigDecimal dineroDisponibleConProyecciones; // Nuevo campo
    private List<CuentaBalanceDTO> cuentas;
}
