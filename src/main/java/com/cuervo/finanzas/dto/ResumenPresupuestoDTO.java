package com.cuervo.finanzas.dto;

import lombok.Data;

@Data
public class ResumenPresupuestoDTO {
    private ConfigPresupuestoDTO config;
    private PresupuestoDTO pptoSemanal;
    private PresupuestoDTO pptoMensual;
}