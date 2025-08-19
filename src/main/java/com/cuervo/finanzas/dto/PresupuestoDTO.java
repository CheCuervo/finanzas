package com.cuervo.finanzas.dto;

import lombok.Data;
import java.util.List;

@Data
public class PresupuestoDTO {
    private PresupuestoDetalleDTO gastos;
    private PresupuestoDetalleDTO ahorros;
    private PresupuestoDetalleDTO inversiones;
    private PresupuestoDetalleDTO disponible;
    private List<ReservaPresupuestoDTO> detalleGastos;
    private List<ReservaPresupuestoDTO> detalleAhorros;
    private List<ReservaPresupuestoDTO> detalleInversiones;
}