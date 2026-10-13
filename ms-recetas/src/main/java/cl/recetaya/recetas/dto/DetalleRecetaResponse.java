package cl.recetaya.recetas.dto;

import cl.recetaya.recetas.model.DetalleReceta;

public record DetalleRecetaResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNombre,
        int cantidad,
        String posologia) {

    public static DetalleRecetaResponse from(DetalleReceta d) {
        return new DetalleRecetaResponse(
                d.getId(),
                d.getMedicamentoId(),
                d.getMedicamentoNombre(),
                d.getCantidad(),
                d.getPosologia());
    }
}
