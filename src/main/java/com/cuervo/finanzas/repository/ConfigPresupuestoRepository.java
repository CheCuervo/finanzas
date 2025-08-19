package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.ConfigPresupuesto;
import com.cuervo.finanzas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigPresupuestoRepository extends JpaRepository<ConfigPresupuesto, Long> {
    Optional<ConfigPresupuesto> findByUser(User user);
}
