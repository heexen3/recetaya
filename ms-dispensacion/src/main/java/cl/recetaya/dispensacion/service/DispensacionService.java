package cl.recetaya.dispensacion.service;

import cl.recetaya.dispensacion.dto.*;
import cl.recetaya.dispensacion.exception.DispensacionException;
import cl.recetaya.dispensacion.exception.RecursoNoEncontradoException;
import cl.recetaya.dispensacion.model.DetalleDispensacion;
import cl.recetaya.dispensacion.model.Dispensacion;
import cl.recetaya.dispensacion.repository.DispensacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class DispensacionService {

    private static final Logger log = LoggerFactory.getLogger(DispensacionService.class);

    private final DispensacionRepository repo;
    private final RestTemplate restTemplate;
    private final String inventarioUrl;
    private final String recetasUrl;

    public DispensacionService(DispensacionRepository repo,
                               RestTemplate restTemplate,
                               @Value("${app.ms-inventario.url}") String inventarioUrl,
                               @Value("${app.ms-recetas.url}") String recetasUrl) {
        this.repo = repo;
        this.restTemplate = restTemplate;
        this.inventarioUrl = inventarioUrl;
        this.recetasUrl = recetasUrl;
    }

    @Transactional(readOnly = true)
    public List<DispensacionResponse> listar() {
        return repo.findAll().stream().map(DispensacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DispensacionResponse obtener(Long id) {
        return DispensacionResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<DispensacionResponse> buscarPorReceta(Long recetaId) {
        return repo.findByRecetaId(recetaId).stream().map(DispensacionResponse::from).toList();
    }

    /**
     * Registra una dispensación:
     * 1. Para cada detalle, descuenta stock en ms-inventario (PUT /api/stock/reservar).
     * 2. Marca la receta como DISPENSADA en ms-recetas (PATCH /api/recetas/{id}/dispensar).
     * 3. Persiste el registro de dispensación.
     */
    @Transactional
    public DispensacionResponse dispensar(DispensacionRequest req) {
        // 1. Descontar stock por cada medicamento
        for (DetalleDispensacionRequest detalle : req.detalles()) {
            descontarStock(req.sucursalId(), detalle.medicamentoId(), detalle.cantidadDispensada());
        }

        // 2. Marcar receta como DISPENSADA
        marcarRecetaDispensada(req.recetaId());

        // 3. Persistir dispensación
        Dispensacion dispensacion = new Dispensacion();
        dispensacion.setRecetaId(req.recetaId());
        dispensacion.setSucursalId(req.sucursalId());
        dispensacion.setFarmaceuticoNombre(req.farmaceuticoNombre());

        req.detalles().forEach(d -> {
            DetalleDispensacion detalle = new DetalleDispensacion();
            detalle.setMedicamentoId(d.medicamentoId());
            detalle.setMedicamentoNombre(d.medicamentoNombre());
            detalle.setCantidadDispensada(d.cantidadDispensada());
            dispensacion.agregarDetalle(detalle);
        });

        return DispensacionResponse.from(repo.save(dispensacion));
    }

    /** Llamada síncrona a ms-inventario para descontar stock. */
    private void descontarStock(Long sucursalId, Long medicamentoId, int cantidad) {
        String url = inventarioUrl + "/api/stock/reservar";
        Map<String, Object> body = Map.of(
                "medicamentoId", medicamentoId,
                "sucursalId", sucursalId,
                "cantidad", cantidad);
        try {
            restTemplate.put(url, body);
            log.info("Stock descontado: medicamento={} sucursal={} cantidad={}", medicamentoId, sucursalId, cantidad);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new DispensacionException("Stock insuficiente para medicamento " + medicamentoId
                    + " en sucursal " + sucursalId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new DispensacionException("No hay registro de stock para medicamento " + medicamentoId
                    + " en sucursal " + sucursalId);
        } catch (Exception ex) {
            throw new DispensacionException("Error al comunicarse con ms-inventario: " + ex.getMessage());
        }
    }

    /** Llamada síncrona a ms-recetas para marcar como dispensada. */
    private void marcarRecetaDispensada(Long recetaId) {
        String url = recetasUrl + "/api/recetas/" + recetaId + "/dispensar";
        try {
            restTemplate.patchForObject(url, null, String.class);
            log.info("Receta {} marcada como DISPENSADA", recetaId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNoEncontradoException("Receta " + recetaId + " no existe en ms-recetas");
        } catch (HttpClientErrorException.Conflict ex) {
            throw new DispensacionException("La receta " + recetaId + " no puede ser dispensada (estado inválido)");
        } catch (Exception ex) {
            throw new DispensacionException("Error al comunicarse con ms-recetas: " + ex.getMessage());
        }
    }

    private Dispensacion buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensación " + id + " no existe"));
    }
}
