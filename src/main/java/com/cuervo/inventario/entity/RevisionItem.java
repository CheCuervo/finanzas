package com.cuervo.inventario.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tfin_revision_items")
@Data
public class RevisionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "revision_id")
    private Revision revision;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto; // Vinculado a tu entidad Producto de inventario

    private boolean revisado; // false por defecto
    private Integer existenciaActual; // Se llena si eligen la X (reponer)
}