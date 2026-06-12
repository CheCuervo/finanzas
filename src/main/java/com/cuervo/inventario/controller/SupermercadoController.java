package com.cuervo.inventario.controller;

import com.cuervo.inventario.dto.SupermercadoRequestDTO;
import com.cuervo.inventario.dto.SupermercadoResponseDTO;
import com.cuervo.inventario.entity.Supermercado;
import com.cuervo.inventario.service.SupermercadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario/supermercados")
@RequiredArgsConstructor
public class SupermercadoController {

    private final SupermercadoService supermercadoService;

    @PostMapping
    public ResponseEntity<SupermercadoResponseDTO> crearSupermercado(@Valid @RequestBody SupermercadoRequestDTO requestDTO) {
        // Cambiamos 'Supermercado' por 'SupermercadoResponseDTO' en la variable y en el ResponseEntity
        SupermercadoResponseDTO nuevoSupermercado = supermercadoService.crearSupermercado(requestDTO);
        return new ResponseEntity<>(nuevoSupermercado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SupermercadoResponseDTO>> obtenerSupermercados() {
        return ResponseEntity.ok(supermercadoService.obtenerSupermercados());
    }
}