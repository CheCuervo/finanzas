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

    @Transactional
    public Revision obtenerOCrearRevisionActiva() {
        Usuario usuario = obtenerUsuarioAutenticado();

        // 1. Validar si ya existe una revisión abierta para este usuario
        return revisionRepository.findByUsuarioAndEstado(usuario, EstadoRevision.ACTIVA)
                .orElseGet(() -> {
                    // 2. Si no existe, creamos una nueva sesión de auditoría
                    Revision nuevaRevision = new Revision();
                    nuevaRevision.setFechaInicio(LocalDateTime.now());
                    nuevaRevision.setEstado(EstadoRevision.ACTIVA);
                    nuevaRevision.setUsuario(usuario);
                    Revision revisionGuardada = revisionRepository.save(nuevaRevision);

                    // 3. Buscamos todos los productos activos del usuario ordenados por ubicación
                    List<Producto> productosActivos = productoRepository
                            .findByUsuarioIdAndActivoTrueOrderByOrdenUbicacionAsc(usuario.getId());

                    // 4. Clonamos los productos en la hoja de progreso de la revisión
                    List<RevisionItem> itemsParaRevisar = new ArrayList<>();
                    for (Producto prod : productosActivos) {
                        RevisionItem item = new RevisionItem();
                        item.setRevision(revisionGuardada);
                        item.setProducto(prod);
                        item.setRevisado(false); // Inician todos pendientes (sin marcar)
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
                throw new NegocioException("El stock actual es obligatorio para reponer el producto.");
            }
            item.setExistenciaActual(stockActual.intValue());

            RevisionProductoRequestDTO requestDTO = new RevisionProductoRequestDTO();
            requestDTO.setProductoId(item.getProducto().getId());
            requestDTO.setStockActual(stockActual);

            listaMercadoService.registrarRevisionProducto(requestDTO);
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
    }

    @Transactional
    public void reordenarProductosDeRevision(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        // Recorremos los IDs enviados por el frente y reasignamos el orden correlativamente
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);

            RevisionItem item = revisionItemRepository.findById(itemId)
                    .orElseThrow(() -> new com.cuervo.finanzas.exception.NegocioException("Item de revisión no encontrado"));

            Producto producto = item.getProducto();
            // Asignamos el nuevo orden consecutivo (1, 2, 3...)
            producto.setOrdenUbicacion(i + 1);
            productoRepository.save(producto);
        }
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

        return revisionGuardada;
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuario no encontrado en el contexto de inventario"));
    }
}