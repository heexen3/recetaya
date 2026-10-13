package cl.recetaya.recetas.service;

import cl.recetaya.recetas.model.Receta;

/**
 * Punto de extensión para publicar el evento "receta emitida".
 * En el diseño objetivo se implementará con Amazon SQS.
 * En esta primera versión la implementación es un simple log.
 */
public interface PublicadorEventos {
    void recetaEmitida(Receta receta);
}
