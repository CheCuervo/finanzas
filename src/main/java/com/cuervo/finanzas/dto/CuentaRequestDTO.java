package com.cuervo.finanzas.dto;

import com.cuervo.finanzas.entity.enums.TipoCuenta;
import lombok.Data;

@Data
public class CuentaRequestDTO {
    private String descripcion;
    private TipoCuenta tipo;
}
