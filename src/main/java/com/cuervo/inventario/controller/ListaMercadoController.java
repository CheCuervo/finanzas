package com.cuervo.inventario.controller;

import com.cuervo.inventario.dto.ListaActivaResponseDTO;
import com.cuervo.inventario.dto.ProductoResponseDTO;
import com.cuervo.inventario.dto.RevisionProductoRequestDTO;
import com.cuervo.inventario.service.ListaMercadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario/lista-mercado")
@RequiredArgsConstructor
public class ListaMercadoController {

    private final ListaMercadoService listaMercadoService;

    @PostMapping("/revision")
    public ResponseEntity<String> registrarRevision(@Valid @RequestBody RevisionProductoRequestDTO request) {
        listaMercadoService.registrarRevisionProducto(request);
        return ResponseEntity.ok("Revisión registrada y lista actualizada correctamente");
    }

    @GetMapping("/activa")
    public ResponseEntity<ListaActivaResponseDTO> obtenerListaActiva() {
        ListaActivaResponseDTO listaActiva = listaMercadoService.obtenerListaActivaAgrupada();
        return ResponseEntity.ok(listaActiva);
    }

    @PutMapping("/cancelar")
    public ResponseEntity<String> cancelarLista() {
        listaMercadoService.cancelarListaActiva();
        return ResponseEntity.ok("Lista de compras cancelada correctamente");
    }

    @PutMapping("/finalizar")
    public ResponseEntity<String> finalizarLista() {
        listaMercadoService.finalizarListaActiva();
        return ResponseEntity.ok("Compra finalizada y lista completada correctamente");
    }

    @PutMapping("/items/{itemId}/comprado")
    public ResponseEntity<String> alternarCompradoItem(
            @PathVariable Long itemId,
            @RequestParam boolean comprado) {
        listaMercadoService.alternarEstadoCompradoItem(itemId, comprado);
        return ResponseEntity.ok("Estado del ítem de mercado actualizado con éxito");
    }

    @GetMapping("/productos-disponibles")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosDisponibles() {
        return ResponseEntity.ok(listaMercadoService.obtenerProductosDisponibles());
    }

    @PostMapping("/items/manual")
    public ResponseEntity<String> agregarProductoManual(
            @RequestParam Long productoId,
            @RequestParam Double cantidad) {
        listaMercadoService.agregarProductoManual(productoId, cantidad);
        return ResponseEntity.ok("Producto agregado manualmente a la lista");
    }

    @PutMapping("/items/{itemId}/cantidad")
    public ResponseEntity<String> actualizarCantidadItem(
            @PathVariable Long itemId,
            @RequestParam Double cantidad) {
        listaMercadoService.actualizarCantidadItem(itemId, cantidad);
        return ResponseEntity.ok("Cantidad del ítem actualizada con éxito");
    }
}