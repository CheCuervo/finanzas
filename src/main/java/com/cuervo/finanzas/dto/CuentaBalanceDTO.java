package com.cuervo.finanzas.dto;

import com.cuervo.finanzas.entity.enums.TipoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CuentaBalanceDTO {
    private Long id;
    private String descripcion;
    private TipoCuenta tipo;
    private BigDecimal balance;
}
