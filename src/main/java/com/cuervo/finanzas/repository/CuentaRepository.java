package com.cuervo.finanzas.repository;

import com.cuervo.finanzas.entity.Cuenta;
import com.cuervo.finanzas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    List<Cuenta> findAllByUser(User user);
    Optional<Cuenta> findByIdAndUser(Long id, User user);
}