package com.cuervo.inventario.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RevisionResponseDTO {
    private Long id;
    private LocalDateTime fechaInicio;
    private String estado; // ACTIVA, FINALIZADA
    private List<RevisionItemResponseDTO> items;
}