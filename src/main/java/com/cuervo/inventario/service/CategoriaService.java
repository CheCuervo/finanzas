package com.cuervo.inventario.service;

import com.cuervo.inventario.dto.CategoriaRequestDTO;
import com.cuervo.inventario.dto.CategoriaResponseDTO;
import com.cuervo.inventario.entity.Categoria;
import com.cuervo.inventario.entity.Usuario;
import com.cuervo.inventario.repository.CategoriaRepository;
import com.cuervo.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO) {
        // 1. Obtener usuario del token
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Buscar o crear usuario en inventario
        Usuario usuarioInventario = usuarioRepository.findByEmail(emailUsuario)
                .orElseGet(() -> {
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(emailUsuario);
                    nuevoUsuario.setNombre(emailUsuario.split("@")[0]);
                    return usuarioRepository.save(nuevoUsuario);
                });

        // 3. Crear entidad Categoria
        Categoria categoria = new Categoria();
        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());
        categoria.setUsuario(usuarioInventario);

        // 4. Guardar en BD
        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        // 5. Mapear a DTO de respuesta
        CategoriaResponseDTO responseDTO = new CategoriaResponseDTO();
        responseDTO.setId(categoriaGuardada.getId());
        responseDTO.setNombre(categoriaGuardada.getNombre());
        responseDTO.setDescripcion(categoriaGuardada.getDescripcion());
        responseDTO.setActivo(categoriaGuardada.getActivo());

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerCategorias() {
        return categoriaRepository.findAll().stream().map(cat -> {
            CategoriaResponseDTO dto = new CategoriaResponseDTO();
            dto.setId(cat.getId());
            dto.setNombre(cat.getNombre());
            dto.setDescripcion(cat.getDescripcion());
            dto.setActivo(cat.getActivo());
            return dto;
        }).collect(Collectors.toList());
    }
}