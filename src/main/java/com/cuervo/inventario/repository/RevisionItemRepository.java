package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.Revision;
import com.cuervo.inventario.entity.RevisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevisionItemRepository extends JpaRepository<RevisionItem, Long> {
    // Verifica si todavía quedan productos con revisado en false para una revisión dada
    boolean existsByRevisionAndRevisadoFalse(Revision revision);
}