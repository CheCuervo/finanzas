package com.cuervo.finanzas.entity;

import com.cuervo.finanzas.entity.enums.TipoReserva;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tfin_reservas")
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nm_id")
    private Long id;

    @Column(name = "ds_concepto", nullable = false)
    private String concepto;

    @Column(name = "nm_valor_meta")
    private BigDecimal valorMeta = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "cd_tipo", nullable = false)
    private TipoReserva tipo;

    @Column(name = "nm_valor_reserva_semanal")
    private BigDecimal valorReservaSemanal = BigDecimal.ZERO;
    
    // --- Campo Renombrado ---
    @Column(name = "fe_meta")
    private LocalDate fechaMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
