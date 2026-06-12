package com.cuervo.inventario.service;

import com.cuervo.inventario.dto.SupermercadoRequestDTO;
import com.cuervo.inventario.dto.SupermercadoResponseDTO; // Importamos el nuevo DTO
import com.cuervo.inventario.entity.Supermercado;
import com.cuervo.inventario.entity.Usuario;
import com.cuervo.inventario.repository.SupermercadoRepository;
import com.cuervo.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupermercadoService {

    private final SupermercadoRepository supermercadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public SupermercadoResponseDTO crearSupermercado(SupermercadoRequestDTO requestDTO) {
        // 1. Obtener el email del usuario autenticado por JWT
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Buscar al usuario en el inventario o crearlo si es su primera vez
        Usuario usuarioInventario = usuarioRepository.findByEmail(emailUsuario)
                .orElseGet(() -> {
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(emailUsuario);
                    nuevoUsuario.setNombre(emailUsuario.split("@")[0]); 
                    return usuarioRepository.save(nuevoUsuario);
                });

        // 3. Crear y asignar datos a la entidad Supermercado
        Supermercado supermercado = new Supermercado();
        supermercado.setNombre(requestDTO.getNombre());
        supermercado.setUbicacion(requestDTO.getUbicacion());
        supermercado.setUsuario(usuarioInventario);

        // 4. Guardar en la base de datos
        Supermercado supermercadoGuardado = supermercadoRepository.save(supermercado);

        // 5. Mapear la entidad guardada al DTO de respuesta
        SupermercadoResponseDTO responseDTO = new SupermercadoResponseDTO();
        responseDTO.setId(supermercadoGuardado.getId());
        responseDTO.setNombre(supermercadoGuardado.getNombre());
        responseDTO.setUbicacion(supermercadoGuardado.getUbicacion());
        responseDTO.setActivo(supermercadoGuardado.getActivo());

        return responseDTO;
    }
}