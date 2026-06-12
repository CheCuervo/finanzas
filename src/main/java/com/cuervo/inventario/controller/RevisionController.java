package com.cuervo.inventario.controller;

import com.cuervo.inventario.entity.Revision;
import com.cuervo.inventario.dto.RevisionResponseDTO;
import com.cuervo.inventario.dto.RevisionItemResponseDTO;
import com.cuervo.inventario.service.RevisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario/revision")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;

    @GetMapping("/activa")
    public ResponseEntity<RevisionResponseDTO> obtenerRevisionActiva() {
        Revision revision = revisionService.obtenerOCrearRevisionActiva();
        return ResponseEntity.ok(mapearADTO(revision));
    }

    @PostMapping("/items/{itemId}/procesar")
    public ResponseEntity<String> procesarItem(
            @PathVariable Long itemId,
            @RequestParam boolean existe,
            @RequestParam(required = false) Double stockActual) {

        revisionService.procesarItemRevision(itemId, existe, stockActual);
        return ResponseEntity.ok("Item de revisión procesado con éxito");
    }

    @PostMapping("/reordenar")
    public ResponseEntity<String> reordenarItems(@RequestBody List<Long> itemIds) {
        revisionService.reordenarProductosDeRevision(itemIds);
        return ResponseEntity.ok("Orden de ubicación actualizado correctamente en la base de datos");
    }

    @PostMapping("/nueva")
    public ResponseEntity<RevisionResponseDTO> crearNuevaRevision() {
        Revision revision = revisionService.crearNuevaRevisionForzada();
        return ResponseEntity.ok(mapearADTO(revision));
    }

    private RevisionResponseDTO mapearADTO(Revision revision) {
        RevisionResponseDTO dto = new RevisionResponseDTO();
        dto.setId(revision.getId());
        dto.setFechaInicio(revision.getFechaInicio());
        dto.setEstado(revision.getEstado() != null ? revision.getEstado().name() : "ACTIVA");

        if (revision.getItems() != null) {
            List<RevisionItemResponseDTO> itemDTOs = revision.getItems().stream()
                    .sorted((a, b) -> {
                        Integer ordenA = (a.getProducto() != null && a.getProducto().getOrdenUbicacion() != null) 
                                ? a.getProducto().getOrdenUbicacion() : 0;
                        Integer ordenB = (b.getProducto() != null && b.getProducto().getOrdenUbicacion() != null) 
                                ? b.getProducto().getOrdenUbicacion() : 0;
                        return ordenA.compareTo(ordenB);
                    })
                    .map(item -> {
                        RevisionItemResponseDTO itemDto = new RevisionItemResponseDTO();
                        itemDto.setId(item.getId());
                        itemDto.setRevisado(item.isRevisado());
                        itemDto.setExistenciaActual(item.getExistenciaActual());
                        
                        if (item.getProducto() != null) {
                            itemDto.setProductoId(item.getProducto().getId());
                            itemDto.setProductoNombre(item.getProducto().getNombre());
                            itemDto.setStockIdeal(item.getProducto().getStockIdeal());
                            itemDto.setStockActual(item.getProducto().getStockActual());
                            itemDto.setStockMinimoSugerido(item.getProducto().getStockMinimoSugerido()); // 👇 NUEVO CAMPO
                            itemDto.setUnidadMedida(item.getProducto().getUnidadMedida().name());
                            itemDto.setObligatorio(item.getProducto().getObligatorio());
                            
                            if (item.getProducto().getCategoria() != null) {
                                itemDto.setCategoriaNombre(item.getProducto().getCategoria().getNombre());
                            }
                            if (item.getProducto().getSupermercado() != null) {
                                itemDto.setSupermercadoNombre(item.getProducto().getSupermercado().getNombre());
                            }
                        }
                        return itemDto;
                    }).toList();
            dto.setItems(itemDTOs);
        }
        return dto;
    }
}