package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.MovimientoGeneralRequestDTO;
import com.cuervo.finanzas.dto.ResumenFinancieroDTO;
import com.cuervo.finanzas.service.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finanzas")
@RequiredArgsConstructor
public class FinanzasController {

    private final FinanzasService finanzasService;

    /**
     * Endpoint para obtener un resumen completo del estado financiero.
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenFinancieroDTO> getResumenFinanciero() {
        ResumenFinancieroDTO resumen = finanzasService.getResumenFinanciero();
        return ResponseEntity.ok(resumen);
    }

    /**
     * Endpoint para registrar un ingreso o egreso directamente en el libro general.
     */
    @PostMapping("/movimientos")
    public ResponseEntity<Void> registrarMovimiento(@RequestBody MovimientoGeneralRequestDTO request) {
        finanzasService.registrarMovimientoGeneral(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint para eliminar un movimiento del libro general.
     */
    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        finanzasService.eliminarMovimiento(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content si es exitoso
    }
}
