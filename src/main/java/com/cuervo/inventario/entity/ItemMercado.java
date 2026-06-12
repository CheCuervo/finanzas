package com.cuervo.inventario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tmer_item_mercado")
public class ItemMercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conectamos el ítem con su lista maestra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_mercado_id", nullable = false)
    private ListaMercado listaMercado;

    // Conectamos el ítem con el producto exacto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Lo que el sistema calculó que debes comprar (Ej: Ideal 5 - Actual 2 = 3)
    @Column(nullable = false, name = "cantidad_sugerida")
    private Double cantidadSugerida;

    // El check que marcarás cuando estés físicamente en el supermercado
    @Column(nullable = false)
    private Boolean comprado_en_super = false; 
}