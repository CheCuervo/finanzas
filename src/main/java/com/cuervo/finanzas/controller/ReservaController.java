package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.MovimientoReservaRequestDTO;
import com.cuervo.finanzas.dto.ReservaMasivaRequestDTO;
import com.cuervo.finanzas.dto.ReservaRequestDTO;
import com.cuervo.finanzas.dto.ResumenReservasDTO;
import com.cuervo.finanzas.entity.Reserva;
import com.cuervo.finanzas.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<Reserva> crearReserva(@RequestBody ReservaRequestDTO request) {
        return new ResponseEntity<>(reservaService.crearReserva(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> consultarReservas() {
        return ResponseEntity.ok(reservaService.consultarReservas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> consultarReservaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.consultarReservaPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reserva> editarReserva(@PathVariable Long id, @RequestBody ReservaRequestDTO request) {
        return ResponseEntity.ok(reservaService.editarReserva(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminarReserva(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para obtener un resumen detallado de las reservas.
     * @param tipo Filtro opcional por tipo de reserva (GASTO_FIJO, AHORRO, ALL).
     * @return Un DTO con los totales y el detalle de las reservas.
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenReservasDTO> getResumenReservas(@RequestParam(defaultValue = "ALL") String tipo) {
        return ResponseEntity.ok(reservaService.getResumenReservas(tipo));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<Void> ingresarMovimiento(@RequestBody MovimientoReservaRequestDTO request) {
        reservaService.ingresarMovimientoReserva(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservas-masivas")
    public ResponseEntity<Void> realizarReservasMasivas(@RequestBody ReservaMasivaRequestDTO request) {
        reservaService.realizarReservasMasivas(request);
        return ResponseEntity.ok().build();
    }
}
