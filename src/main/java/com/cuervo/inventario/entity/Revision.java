package com.cuervo.inventario.entity;

import com.cuervo.inventario.entity.enums.EstadoRevision;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tfin_revisiones")
@Data
public class Revision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaInicio;
    
    @Enumerated(EnumType.STRING)
    private EstadoRevision estado; // ACTIVA, FINALIZADA

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Vinculado a tu entidad User de inventario

    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RevisionItem> items;
}