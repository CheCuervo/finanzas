package com.cuervo.finanzas.dto;

import com.cuervo.finanzas.entity.enums.TipoReserva;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReservaRequestDTO {
    private String concepto;
    private BigDecimal valorMeta;
    private TipoReserva tipo;
    private BigDecimal valorReservaSemanal;
    private LocalDate fechaMeta;
}
