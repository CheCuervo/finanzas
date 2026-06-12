package com.cuervo.inventario.repository;

import com.cuervo.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Este método será vital para el flujo de autenticación e inventario
    Optional<Usuario> findByEmail(String email);
}