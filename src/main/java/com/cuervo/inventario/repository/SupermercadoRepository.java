package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.Supermercado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupermercadoRepository extends JpaRepository<Supermercado, Long> {
    
    List<Supermercado> findByActivoTrue();
}