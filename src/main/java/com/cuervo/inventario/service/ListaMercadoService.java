package com.cuervo.inventario.service;

import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.inventario.dto.*;
import com.cuervo.inventario.entity.*;
import com.cuervo.inventario.entity.enums.EstadoLista;
import com.cuervo.inventario.repository.*;
import com.cuervo.inventario.handler.NotificationWebSocketHandler; // 🔥 Sincronización en tiempo real
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListaMercadoService {

    private final ListaMercadoRepository listaMercadoRepository;
    private final ItemMercadoRepository itemMercadoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler; // 🔥 WebSocket inyectado

    @Transactional
    public void registrarRevisionProducto(RevisionProductoRequestDTO request) {
        Usuario usuario = obtenerUsuarioAutenticado();

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new NegocioException("Producto no encontrado"));

        producto.setStockActual(request.getStockActual());
        productoRepository.save(producto);

        double cantidadSugerida = producto.getStockIdeal() - request.getStockActual();
        ListaMercado listaActiva = obtenerOCrearListaActiva(usuario);

        Optional<ItemMercado> itemExistente = itemMercadoRepository
                .findByListaMercadoIdAndProductoId(listaActiva.getId(), producto.getId());

        if (cantidadSugerida > 0) {
            ItemMercado item = itemExistente.orElse(new ItemMercado());
            item.setListaMercado(listaActiva);
            item.setProducto(producto);
            item.setCantidadSugerida(cantidadSugerida);
            itemMercadoRepository.save(item);
        } else {
            itemExistente.ifPresent(itemMercadoRepository::delete);
        }

        // Avisa a otros dispositivos que la lista cambió
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional(readOnly = true)
    public ListaActivaResponseDTO obtenerListaActivaAgrupada() {
        Usuario usuario = obtenerUsuarioAutenticado();

        java.util.Optional<ListaMercado> listaActivaOpt = listaMercadoRepository
                .findByUsuarioIdAndEstado(usuario.getId(), EstadoLista.ACTIVA);

        ListaActivaResponseDTO response = new ListaActivaResponseDTO();

        if (listaActivaOpt.isEmpty()) {
            response.setListaId(null);
            response.setNombre("Sin lista activa");
            response.setFechaCreacion(java.time.LocalDateTime.now());
            response.setSupermercados(new java.util.ArrayList<>());
            return response;
        }

        ListaMercado listaActiva = listaActivaOpt.get();
        List<ItemMercado> items = itemMercadoRepository.findByListaMercadoId(listaActiva.getId());

        Map<String, Map<String, List<ItemResumenDTO>>> agrupacion = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProducto().getSupermercado().getNombre(),
                        Collectors.groupingBy(
                                item -> item.getProducto().getCategoria().getNombre(),
                                Collectors.mapping(item -> new ItemResumenDTO(
                                        item.getId(),
                                        item.getProducto().getNombre(),
                                        item.getCantidadSugerida(),
                                        item.getProducto().getUnidadMedida(),
                                        item.getComprado_en_super(),
                                        item.getProducto().getOrdenSupermercado() // Constructor optimizado de 6 campos
                                ), Collectors.toList())
                        )
                ));

        List<SupermercadoAgrupadoDTO> supermercadosAgrupados = agrupacion.entrySet().stream()
                .map(entrySuper -> {
                    List<CategoriaAgrupadaDTO> categoriasAgrupadas = entrySuper.getValue().entrySet().stream()
                            .map(entryCat -> new CategoriaAgrupadaDTO(entryCat.getKey(), entryCat.getValue()))
                            .collect(Collectors.toList());
                    return new SupermercadoAgrupadoDTO(entrySuper.getKey(), categoriasAgrupadas);
                })
                .collect(Collectors.toList());

        response.setListaId(listaActiva.getId());
        response.setNombre(listaActiva.getNombre());
        response.setFechaCreacion(listaActiva.getFechaCreacion());
        response.setSupermercados(supermercadosAgrupados);

        return response;
    }

    @Transactional
    public void cancelarListaActiva() {
        Usuario usuario = obtenerUsuarioAutenticado();
        ListaMercado listaActiva = listaMercadoRepository
                .findByUsuarioIdAndEstado(usuario.getId(), EstadoLista.ACTIVA)
                .orElseThrow(() -> new NegocioException("No hay ninguna lista de mercado activa para cancelar"));

        listaActiva.setEstado(EstadoLista.CANCELADA);
        listaMercadoRepository.save(listaActiva);

        // Notifica la cancelación en tiempo real
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional
    public void finalizarListaActiva() {
        Usuario usuario = obtenerUsuarioAutenticado();
        ListaMercado listaActiva = listaMercadoRepository
                .findByUsuarioIdAndEstado(usuario.getId(), EstadoLista.ACTIVA)
                .orElseThrow(() -> new NegocioException("No hay ninguna lista de mercado activa para finalizar"));

        listaActiva.setEstado(EstadoLista.COMPLETADA);
        listaMercadoRepository.save(listaActiva);

        // Notifica la finalización en tiempo real
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional
    public void alternarEstadoCompradoItem(Long itemId, boolean comprado) {
        ItemMercado item = itemMercadoRepository.findById(itemId)
                .orElseThrow(() -> new NegocioException("Ítem de mercado no encontrado"));
        item.setComprado_en_super(comprado);
        itemMercadoRepository.save(item);

        // Sincroniza el check de compra en los demás teléfonos
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional
    public void eliminarProductoDeListaActiva(Producto producto, Usuario usuario) {
        listaMercadoRepository.findByUsuarioIdAndEstado(usuario.getId(), EstadoLista.ACTIVA)
                .ifPresent(listaActiva -> {
                    itemMercadoRepository.findByListaMercadoIdAndProductoId(listaActiva.getId(), producto.getId())
                            .ifPresent(item -> {
                                itemMercadoRepository.delete(item);
                                notificationWebSocketHandler.broadcast("refresh-lista");
                            });
                });
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosDisponibles() {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<Producto> productos = productoRepository.findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(usuario.getId());
        return productos.stream().map(p -> {
            ProductoResponseDTO dto = new ProductoResponseDTO();
            dto.setId(p.getId());
            dto.setNombre(p.getNombre());
            dto.setUnidadMedida(p.getUnidadMedida());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void agregarProductoManual(Long productoId, Double cantidad) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NegocioException("Producto no encontrado"));

        ListaMercado listaActiva = obtenerOCrearListaActiva(usuario);

        Optional<ItemMercado> itemExistente = itemMercadoRepository
                .findByListaMercadoIdAndProductoId(listaActiva.getId(), producto.getId());

        ItemMercado item = itemExistente.orElse(new ItemMercado());
        item.setListaMercado(listaActiva);
        item.setProducto(producto);

        if (item.getCantidadSugerida() != null) {
            item.setCantidadSugerida(item.getCantidadSugerida() + cantidad);
        } else {
            item.setCantidadSugerida(cantidad);
        }
        itemMercadoRepository.save(item);

        // Actualiza la lista en vivo
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional
    public void actualizarCantidadItem(Long itemId, Double nuevaCantidad) {
        ItemMercado item = itemMercadoRepository.findById(itemId)
                .orElseThrow(() -> new NegocioException("Ítem de mercado no encontrado"));

        if (nuevaCantidad <= 0) {
            itemMercadoRepository.delete(item);
        } else {
            item.setCantidadSugerida(nuevaCantidad);
            itemMercadoRepository.save(item);
        }

        // Refresca las cantidades de forma reactiva en otros navegadores
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    @Transactional
    public void registrarRevisionProductoConCantidadExacta(Long productoId, Double stockActual, Double cantidadAComprar) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NegocioException("Producto no encontrado"));

        producto.setStockActual(stockActual);
        productoRepository.save(producto);

        ListaMercado listaActiva = obtenerOCrearListaActiva(usuario);
        java.util.Optional<ItemMercado> itemExistente = itemMercadoRepository
                .findByListaMercadoIdAndProductoId(listaActiva.getId(), producto.getId());

        if (cantidadAComprar > 0) {
            ItemMercado item = itemExistente.orElse(new ItemMercado());
            item.setListaMercado(listaActiva);
            item.setProducto(producto);
            item.setCantidadSugerida(cantidadAComprar);
            itemMercadoRepository.save(item);
        } else {
            itemExistente.ifPresent(itemMercadoRepository::delete);
        }

        // Sincroniza adiciones hechas por revisión
        notificationWebSocketHandler.broadcast("refresh-lista");
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado en el contexto de inventario"));
    }

    private ListaMercado obtenerOCrearListaActiva(Usuario usuario) {
        return listaMercadoRepository.findByUsuarioIdAndEstado(usuario.getId(), EstadoLista.ACTIVA)
                .orElseGet(() -> {
                    final String prefijo = "Lista de Mercado - ";
                    ListaMercado nuevaLista = new ListaMercado();
                    nuevaLista.setNombre(prefijo + java.time.LocalDate.now().toString());
                    nuevaLista.setUsuario(usuario);
                    nuevaLista.setEstado(EstadoLista.ACTIVA);
                    return listaMercadoRepository.save(nuevaLista);
                });
    }
}