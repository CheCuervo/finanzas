package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.ItemMercado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemMercadoRepository extends JpaRepository<ItemMercado, Long> {
    List<ItemMercado> findByListaMercadoId(Long listaMercadoId);
    
    // Para verificar si un producto ya está en la lista actual y solo actualizar su cantidad
    Optional<ItemMercado> findByListaMercadoIdAndProductoId(Long listaMercadoId, Long productoId);
}