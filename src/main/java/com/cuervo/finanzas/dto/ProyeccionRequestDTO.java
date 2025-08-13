package com.cuervo.finanzas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProyeccionRequestDTO {
    private String concepto;
    private BigDecimal valor;
}
