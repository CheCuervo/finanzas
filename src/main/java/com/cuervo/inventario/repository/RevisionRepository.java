package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.Revision;
import com.cuervo.inventario.entity.enums.EstadoRevision;
import com.cuervo.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RevisionRepository extends JpaRepository<Revision, Long> {
    // Busca si el usuario ya tiene una revisión en estado ACTIVA
    Optional<Revision> findByUsuarioAndEstado(Usuario usuario, EstadoRevision estado);
}