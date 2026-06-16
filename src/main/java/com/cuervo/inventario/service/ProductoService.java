package com.cuervo.inventario.service;

import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.inventario.dto.ProductoRequestDTO;
import com.cuervo.inventario.dto.ProductoResponseDTO;
import com.cuervo.inventario.entity.Categoria;
import com.cuervo.inventario.entity.Producto;
import com.cuervo.inventario.entity.Supermercado;
import com.cuervo.inventario.entity.Usuario;
import com.cuervo.inventario.repository.CategoriaRepository;
import com.cuervo.inventario.repository.ProductoRepository;
import com.cuervo.inventario.repository.SupermercadoRepository;
import com.cuervo.inventario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SupermercadoRepository supermercadoRepository;
    private final RevisionService revisionService;

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO requestDTO) {
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseGet(() -> {
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(emailUsuario);
                    nuevoUsuario.setNombre(emailUsuario.split("@")[0]);
                    return usuarioRepository.save(nuevoUsuario);
                });

        Categoria categoria = categoriaRepository.findById(requestDTO.getCategoriaId())
                .orElseThrow(() -> new NegocioException("Categoría no encontrada"));

        Supermercado supermercado = supermercadoRepository.findById(requestDTO.getSupermercadoId())
                .orElseThrow(() -> new NegocioException("Supermercado no encontrado"));

        Producto producto = new Producto();
        producto.setNombre(requestDTO.getNombre());
        producto.setStockIdeal(requestDTO.getStockIdeal());
        producto.setStockMinimoSugerido(requestDTO.getStockMinimoSugerido());
        producto.setUnidadMedida(requestDTO.getUnidadMedida());
        producto.setObligatorio(requestDTO.getObligatorio());
        producto.setCategoria(categoria);
        producto.setSupermercado(supermercado);
        producto.setUsuario(usuario);

        // 🔥 TRUCO DE USABILIDAD: Seteamos un 0 temporal para saciar el NOT NULL de la Base de Datos
        producto.setOrdenUbicacion(0);
        producto.setOrdenSupermercado(0);

        // 1. Guardamos el producto (Ahora Postgres sí aceptará el INSERT porque no lleva nulls)
        Producto productoGuardado = productoRepository.save(producto);

        // 2. Tomamos el ID real autogenerado y sobreescribimos el 0 temporal
        Integer idComoOrden = productoGuardado.getId().intValue();
        productoGuardado.setOrdenUbicacion(idComoOrden);
        productoGuardado.setOrdenSupermercado(idComoOrden);

        // Vinculamos el artículo a la revisión actual de la alacena si está abierta
        revisionService.agregarProductoARevisionActivaSiExiste(productoGuardado, usuario);

        // Gracias a @Transactional, Hibernate hará el UPDATE definitivo en la BD automáticamente aquí
        return mapearADto(productoGuardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerMisProductos() {
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElse(null);

        if (usuario == null) {
            return List.of();
        }

        List<Producto> productos = productoRepository.findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(usuario.getId());

        return productos.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    private ProductoResponseDTO mapearADto(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setStockIdeal(producto.getStockIdeal());
        dto.setStockActual(producto.getStockActual());
        dto.setStockMinimoSugerido(producto.getStockMinimoSugerido());
        dto.setUnidadMedida(producto.getUnidadMedida());
        dto.setOrdenUbicacion(producto.getOrdenUbicacion());
        dto.setOrdenSupermercado(producto.getOrdenSupermercado());
        dto.setActivo(producto.getActivo());
        dto.setObligatorio(producto.getObligatorio());

        dto.setCategoriaId(producto.getCategoria().getId());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());

        dto.setSupermercadoId(producto.getSupermercado().getId());
        dto.setSupermercadoNombre(producto.getSupermercado().getNombre());

        return dto;
    }
}