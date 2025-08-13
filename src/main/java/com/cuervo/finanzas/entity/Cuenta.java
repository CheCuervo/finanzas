package com.cuervo.finanzas.entity;

import com.cuervo.finanzas.entity.enums.TipoCuenta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tfin_cuentas")
@Data
public class Cuenta {
    // ... campos existentes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @Column(name = "ds_descripcion", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "cd_tipo", nullable = false)
    private TipoCuenta tipo;

    // --- Nueva Relación ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Para evitar bucles infinitos en la serialización
    private User user;
}