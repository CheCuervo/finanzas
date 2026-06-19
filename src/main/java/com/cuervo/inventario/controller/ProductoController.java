package com.cuervo.inventario.controller;

import com.cuervo.inventario.dto.ProductoRequestDTO;
import com.cuervo.inventario.dto.ProductoResponseDTO;
import com.cuervo.inventario.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO nuevoProducto = productoService.crearProducto(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerMisProductos() {
        List<ProductoResponseDTO> productos = productoService.obtenerMisProductos();
        return ResponseEntity.ok(productos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoRequestDTO request) {

        // Aquí llamas a tu servicio para actualizar el producto
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }
}