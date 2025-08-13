package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.Reserva;
import com.cuervo.finanzas.entity.User;
import com.cuervo.finanzas.entity.enums.TipoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllByUser(User user);
    Optional<Reserva> findByIdAndUser(Long id, User user);

    /**
     * Suma el valor de todos los movimientos de tipo 'Reserva' para un usuario,
     * filtrando por el tipo de reserva (GASTO_FIJO o AHORRO).
     */
    @Query("SELECT COALESCE(SUM(lr.valor), 0) FROM LibroReserva lr WHERE lr.reserva.user = :user AND lr.reserva.tipo = :tipo AND lr.tipoMovimiento = 'Reserva'")
    BigDecimal sumTotalReservadoByTipoAndUser(@Param("user") User user, @Param("tipo") TipoReserva tipo);
}
