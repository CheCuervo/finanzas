package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.ConfigPresupuestoRequestDTO;
import com.cuervo.finanzas.dto.ResumenPresupuestoDTO;
import com.cuervo.finanzas.entity.ConfigPresupuesto;
import com.cuervo.finanzas.service.PresupuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presupuesto")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenPresupuestoDTO> getResumenPresupuesto() {
        return ResponseEntity.ok(presupuestoService.getResumenPresupuesto());
    }

    @PostMapping("/config")
    public ResponseEntity<ConfigPresupuesto> guardarConfigPresupuesto(@RequestBody ConfigPresupuestoRequestDTO request) {
        ConfigPresupuesto configGuardada = presupuestoService.guardarConfigPresupuesto(request);
        return ResponseEntity.ok(configGuardada);
    }
}
