package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.ListaMercado;
import com.cuervo.inventario.entity.enums.EstadoLista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListaMercadoRepository extends JpaRepository<ListaMercado, Long> {
    // Buscar la lista ACTIVA del usuario
    Optional<ListaMercado> findByUsuarioIdAndEstado(Long usuarioId, EstadoLista estado);
}