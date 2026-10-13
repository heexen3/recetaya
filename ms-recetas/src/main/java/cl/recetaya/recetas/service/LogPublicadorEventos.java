package cl.recetaya.recetas.service;

import cl.recetaya.recetas.model.Receta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación simple que registra el evento en el log.
 * Se reemplazará por SqsPublicadorEventos cuando se integre AWS.
 */
@Component
public class LogPublicadorEventos implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(LogPublicadorEventos.class);

    @Override
    public void recetaEmitida(Receta receta) {
        log.info("EVENTO: receta emitida id={} paciente={} detalles={}",
                receta.getId(), receta.getPacienteRut(), receta.getDetalles().size());
    }
}
