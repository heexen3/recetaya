package cl.recetaya.recetas.service;

import cl.recetaya.recetas.dto.RecetaRequest;
import cl.recetaya.recetas.dto.RecetaResponse;
import cl.recetaya.recetas.exception.EstadoInvalidoException;
import cl.recetaya.recetas.exception.RecursoNoEncontradoException;
import cl.recetaya.recetas.model.DetalleReceta;
import cl.recetaya.recetas.model.EstadoReceta;
import cl.recetaya.recetas.model.Receta;
import cl.recetaya.recetas.repository.RecetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecetaService {

    private static final Logger log = LoggerFactory.getLogger(RecetaService.class);

    private final RecetaRepository repo;
    private final PublicadorEventos publicador;

    public RecetaService(RecetaRepository repo, PublicadorEventos publicador) {
        this.repo = repo;
        this.publicador = publicador;
    }

    @Transactional(readOnly = true)
    public List<RecetaResponse> listar() {
        return repo.findAll().stream().map(RecetaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RecetaResponse obtener(Long id) {
        return RecetaResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<RecetaResponse> buscarPorPaciente(String rut) {
        return repo.findByPacienteRut(rut).stream().map(RecetaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecetaResponse> buscarPorEstado(EstadoReceta estado) {
        return repo.findByEstado(estado).stream().map(RecetaResponse::from).toList();
    }

    /** Emite una receta y publica el evento sin bloquear la emisión. */
    @Transactional
    public RecetaResponse emitir(RecetaRequest req) {
        Receta receta = new Receta();
        receta.setPacienteNombre(req.pacienteNombre());
        receta.setPacienteRut(req.pacienteRut());
        receta.setMedicoNombre(req.medicoNombre());
        receta.setDiagnostico(req.diagnostico());
        receta.setEstado(EstadoReceta.EMITIDA);

        req.detalles().forEach(d -> {
            DetalleReceta detalle = new DetalleReceta();
            detalle.setMedicamentoId(d.medicamentoId());
            detalle.setMedicamentoNombre(d.medicamentoNombre());
            detalle.setCantidad(d.cantidad());
            detalle.setPosologia(d.posologia());
            receta.agregarDetalle(detalle);
        });

        Receta guardada = repo.save(receta);

        // Publica el evento; si falla, no revierte la receta
        try {
            publicador.recetaEmitida(guardada);
        } catch (Exception ex) {
            log.warn("No se pudo publicar el evento de receta {}: {}", guardada.getId(), ex.getMessage());
        }

        return RecetaResponse.from(guardada);
    }

    /** Marca la receta como DISPENSADA. Solo si está EMITIDA. */
    @Transactional
    public RecetaResponse marcarDispensada(Long id) {
        Receta r = buscar(id);
        validarTransicion(r, EstadoReceta.DISPENSADA);
        r.setEstado(EstadoReceta.DISPENSADA);
        return RecetaResponse.from(repo.save(r));
    }

    /** Marca la receta como ANULADA. Solo si está EMITIDA. */
    @Transactional
    public RecetaResponse anular(Long id) {
        Receta r = buscar(id);
        validarTransicion(r, EstadoReceta.ANULADA);
        r.setEstado(EstadoReceta.ANULADA);
        return RecetaResponse.from(repo.save(r));
    }

    private Receta buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Receta " + id + " no existe"));
    }

    private void validarTransicion(Receta r, EstadoReceta destino) {
        if (r.getEstado() != EstadoReceta.EMITIDA) {
            throw new EstadoInvalidoException(
                    "La receta " + r.getId() + " no se puede pasar a " + destino
                            + " porque está en estado " + r.getEstado());
        }
    }
}
