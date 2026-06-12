package com.cuervo.inventario.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class SupermercadoAgrupadoDTO {
    private String supermercadoNombre;
    private List<CategoriaAgrupadaDTO> categorias;
}