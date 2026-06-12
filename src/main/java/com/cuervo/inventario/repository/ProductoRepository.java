package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(Long usuarioId);
}