package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.CuentaBalanceDTO;
import com.cuervo.finanzas.dto.CuentaRequestDTO;
import com.cuervo.finanzas.dto.MovimientoDTO;
import com.cuervo.finanzas.dto.ReajusteCuentaRequestDTO;
import com.cuervo.finanzas.entity.Cuenta;
import com.cuervo.finanzas.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
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
    public ResponseEntity<List<CuentaBalanceDTO>> consultarCuentas() {
        return ResponseEntity.ok(cuentaService.consultarCuentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaBalanceDTO> consultarCuentaPorId(@PathVariable Long id) {
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

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<Page<MovimientoDTO>> consultarMovimientos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {

        // Si no se provee mes y año, se usan los del mes en curso.
        YearMonth currentYearMonth = YearMonth.now();
        int mesAFiltrar = mes != null ? mes : currentYearMonth.getMonthValue();
        int anioAFiltrar = anio != null ? anio : currentYearMonth.getYear();

        Page<MovimientoDTO> movimientos = cuentaService.consultarMovimientosPorCuenta(id, anioAFiltrar, mesAFiltrar, page, size);
        return ResponseEntity.ok(movimientos);
    }
}
