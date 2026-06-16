package com.cuervo.inventario.service;

import com.cuervo.finanzas.exception.NegocioException;
import com.cuervo.inventario.entity.Revision;
import com.cuervo.inventario.entity.RevisionItem;
import com.cuervo.inventario.entity.Producto;
import com.cuervo.inventario.entity.Usuario;
import com.cuervo.inventario.entity.enums.EstadoRevision;
import com.cuervo.inventario.dto.RevisionProductoRequestDTO;
import com.cuervo.inventario.repository.RevisionRepository;
import com.cuervo.inventario.repository.RevisionItemRepository;
import com.cuervo.inventario.repository.ProductoRepository;
import com.cuervo.inventario.repository.UsuarioRepository;
import com.cuervo.inventario.handler.NotificationWebSocketHandler; // 🔥 Sincronización en tiempo real
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final RevisionItemRepository revisionItemRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ListaMercadoService listaMercadoService;
    private final NotificationWebSocketHandler notificationWebSocketHandler; // 🔥 WebSocket inyectado

    @Transactional
    public Revision obtenerOCrearRevisionActiva() {
        Usuario usuario = obtenerUsuarioAutenticado();

        return revisionRepository.findByUsuarioAndEstado(usuario, EstadoRevision.ACTIVA)
                .orElseGet(() -> {
                    Revision nuevaRevision = new Revision();
                    nuevaRevision.setFechaInicio(LocalDateTime.now());
                    nuevaRevision.setEstado(EstadoRevision.ACTIVA);
                    nuevaRevision.setUsuario(usuario);
                    Revision revisionGuardada = revisionRepository.save(nuevaRevision);

                    List<Producto> productosActivos = productoRepository
                            .findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(usuario.getId());

                    List<RevisionItem> itemsParaRevisar = new ArrayList<>();
                    for (Producto prod : productosActivos) {
                        RevisionItem item = new RevisionItem();
                        item.setRevision(revisionGuardada);
                        item.setProducto(prod);
                        item.setRevisado(false);
                        itemsParaRevisar.add(item);
                    }

                    revisionItemRepository.saveAll(itemsParaRevisar);
                    revisionGuardada.setItems(itemsParaRevisar);

                    return revisionGuardada;
                });
    }

    @Transactional
    public void procesarItemRevision(Long itemId, boolean existe, Double stockActual) {
        RevisionItem item = revisionItemRepository.findById(itemId)
                .orElseThrow(() -> new NegocioException("Item de revisión no encontrado"));

        item.setRevisado(true);
        Revision revision = item.getRevision();

        if (!existe) {
            if (stockActual == null) {
                throw new NegocioException("La cantidad es obligatoria.");
            }

            item.setExistenciaActual(item.getProducto().getStockActual() != null ? item.getProducto().getStockActual().intValue() : 0);

            listaMercadoService.registrarRevisionProductoConCantidadExacta(
                    item.getProducto().getId(),
                    item.getProducto().getStockActual(),
                    stockActual
            );
        } else {
            Producto producto = item.getProducto();
            item.setExistenciaActual(producto.getStockIdeal().intValue());
            producto.setStockActual(producto.getStockIdeal());
            productoRepository.save(producto);

            listaMercadoService.eliminarProductoDeListaActiva(producto, revision.getUsuario());
        }

        revisionItemRepository.save(item);

        boolean quedanPendientes = revisionItemRepository.existsByRevisionAndRevisadoFalse(revision);

        if (!quedanPendientes) {
            revision.setEstado(EstadoRevision.FINALIZADA);
            revisionRepository.save(revision);
        }

        // 🔥 NOTIFICACIONES WEBSOCKET BIDIRECCIONALES
        notificationWebSocketHandler.broadcast("refresh-revision");
        if (!existe) {
            notificationWebSocketHandler.broadcast("refresh-lista");
        }
    }

    @Transactional
    public void reordenarProductosDeRevision(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);

            RevisionItem item = revisionItemRepository.findById(itemId)
                    .orElseThrow(() -> new NegocioException("Item de revisión no encontrado"));

            Producto producto = item.getProducto();
            producto.setOrdenUbicacion(i + 1);
            productoRepository.save(producto);
        }

        // Refresca el orden visual del Drag and Drop en otros celulares al instante
        notificationWebSocketHandler.broadcast("refresh-revision");
    }

    @Transactional
    public Revision crearNuevaRevisionForzada() {
        Usuario usuario = obtenerUsuarioAutenticado();

        revisionRepository.findByUsuarioAndEstado(usuario, EstadoRevision.ACTIVA)
                .ifPresent(rev -> {
                    rev.setEstado(EstadoRevision.FINALIZADA);
                    revisionRepository.save(rev);
                });

        Revision nuevaRevision = new Revision();
        nuevaRevision.setFechaInicio(LocalDateTime.now());
        nuevaRevision.setEstado(EstadoRevision.ACTIVA);
        nuevaRevision.setUsuario(usuario);
        Revision revisionGuardada = revisionRepository.save(nuevaRevision);

        List<Producto> productosActivos = productoRepository
                .findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(usuario.getId());

        List<RevisionItem> itemsParaRevisar = new ArrayList<>();
        for (Producto prod : productosActivos) {
            RevisionItem item = new RevisionItem();
            item.setRevision(revisionGuardada);
            item.setProducto(prod);
            item.setRevisado(false);
            itemsParaRevisar.add(item);
        }

        revisionItemRepository.saveAll(itemsParaRevisar);
        revisionGuardada.setItems(itemsParaRevisar);

        // Notifica el reinicio de la hoja de auditoría
        notificationWebSocketHandler.broadcast("refresh-revision");
        return revisionGuardada;
    }

    @Transactional
    public void agregarProductoARevisionActivaSiExiste(Producto producto, Usuario usuario) {
        revisionRepository.findByUsuarioAndEstado(usuario, EstadoRevision.ACTIVA)
                .ifPresent(revisionActiva -> {
                    RevisionItem nuevoItem = new RevisionItem();
                    nuevoItem.setRevision(revisionActiva);
                    nuevoItem.setProducto(producto);
                    nuevoItem.setRevisado(false);

                    revisionItemRepository.save(nuevoItem);

                    // Sincroniza la adición del producto nuevo en caliente
                    notificationWebSocketHandler.broadcast("refresh-revision");
                });
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado en el contexto de inventario"));
    }
}