package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.Proyeccion;
import com.cuervo.finanzas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProyeccionRepository extends JpaRepository<Proyeccion, Long> {
    List<Proyeccion> findAllByUser(User user);
    Optional<Proyeccion> findByIdAndUser(Long id, User user);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Proyeccion p WHERE p.user = :user")
    BigDecimal sumTotalProyeccionesByUser(@Param("user") User user);
}