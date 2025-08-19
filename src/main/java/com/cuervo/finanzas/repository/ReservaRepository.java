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

    @Query("SELECT COALESCE(SUM(lr.valor), 0) FROM LibroReserva lr WHERE lr.reserva.user = :user AND lr.reserva.tipo = :tipo AND lr.tipoMovimiento = 'Reserva'")
    BigDecimal sumTotalReservadoByTipoAndUser(@Param("user") User user, @Param("tipo") TipoReserva tipo);

    /**
     * Suma el valor de la reserva semanal para un usuario, filtrando por el tipo de reserva.
     */
    @Query("SELECT COALESCE(SUM(r.valorReservaSemanal), 0) FROM Reserva r WHERE r.user = :user AND r.tipo = :tipo")
    BigDecimal sumValorReservaSemanalByUserAndTipo(@Param("user") User user, @Param("tipo") TipoReserva tipo);

    /**
     * Suma el valor total de la reserva semanal para un usuario.
     */
    @Query("SELECT COALESCE(SUM(r.valorReservaSemanal), 0) FROM Reserva r WHERE r.user = :user")
    BigDecimal sumTotalValorReservaSemanalByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(r.valorReservaSemanal), 0) FROM Reserva r WHERE r.user = :user AND r.tipo IN (com.cuervo.finanzas.entity.enums.TipoReserva.GASTO_FIJO, com.cuervo.finanzas.entity.enums.TipoReserva.GASTO_FIJO_MES)")
    BigDecimal sumValorReservaSemanalForGastos(@Param("user") User user);
}
