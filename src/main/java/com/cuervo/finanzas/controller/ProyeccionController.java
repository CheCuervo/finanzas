package com.cuervo.finanzas.controller;

import com.cuervo.finanzas.dto.ProyeccionRequestDTO;
import com.cuervo.finanzas.entity.Proyeccion;
import com.cuervo.finanzas.service.ProyeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyecciones")
@RequiredArgsConstructor
public class ProyeccionController {

    private final ProyeccionService proyeccionService;

    @PostMapping
    public ResponseEntity<Proyeccion> crearProyeccion(@RequestBody ProyeccionRequestDTO request) {
        Proyeccion proyeccionCreada = proyeccionService.crearProyeccion(request);
        return new ResponseEntity<>(proyeccionCreada, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Proyeccion>> consultarProyecciones() {
        List<Proyeccion> proyecciones = proyeccionService.consultarProyecciones();
        return ResponseEntity.ok(proyecciones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyeccion> editarProyeccion(@PathVariable Long id, @RequestBody ProyeccionRequestDTO request) {
        Proyeccion proyeccionEditada = proyeccionService.editarProyeccion(id, request);
        return ResponseEntity.ok(proyeccionEditada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyeccion(@PathVariable Long id) {
        proyeccionService.eliminarProyeccion(id);
        return ResponseEntity.noContent().build();
    }
}
