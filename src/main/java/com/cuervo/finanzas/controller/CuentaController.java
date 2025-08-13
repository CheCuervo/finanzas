package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.CuentaRequestDTO;
import com.cuervo.finanzas.dto.ReajusteCuentaRequestDTO;
import com.cuervo.finanzas.entity.Cuenta;
import com.cuervo.finanzas.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody CuentaRequestDTO request) {
        Cuenta cuentaCreada = cuentaService.crearCuenta(request);
        return new ResponseEntity<>(cuentaCreada, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cuenta>> consultarCuentas() {
        return ResponseEntity.ok(cuentaService.consultarCuentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> consultarCuentaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.consultarCuentaPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cuenta> editarCuenta(@PathVariable Long id, @RequestBody CuentaRequestDTO request) {
        return ResponseEntity.ok(cuentaService.editarCuenta(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long id) {
        cuentaService.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reajustar")
    public ResponseEntity<Void> reajustarCuenta(@RequestBody ReajusteCuentaRequestDTO request) {
        cuentaService.reajustarCuenta(request);
        return ResponseEntity.ok().build();
    }
}
