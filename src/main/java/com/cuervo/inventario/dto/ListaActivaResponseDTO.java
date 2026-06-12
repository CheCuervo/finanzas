package com.cuervo.inventario.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListaActivaResponseDTO {
    private Long listaId;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private List<SupermercadoAgrupadoDTO> supermercados;
}