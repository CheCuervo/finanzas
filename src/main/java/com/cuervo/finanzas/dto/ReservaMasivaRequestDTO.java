package com.cuervo.finanzas.dto;

import lombok.Data;

@Data
public class ReservaMasivaRequestDTO {
    private Integer nmSemanas;
    private String concepto;
    private String tipoReserva; // "AHORRO", "GASTO_FIJO", o "ALL"
    private Long idCuenta; // Cuenta de origen para la reserva
}
