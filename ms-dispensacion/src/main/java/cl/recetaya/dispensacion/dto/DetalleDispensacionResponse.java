package cl.recetaya.dispensacion.dto;

import cl.recetaya.dispensacion.model.DetalleDispensacion;

public record DetalleDispensacionResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNombre,
        int cantidadDispensada) {

    public static DetalleDispensacionResponse from(DetalleDispensacion d) {
        return new DetalleDispensacionResponse(
                d.getId(),
                d.getMedicamentoId(),
                d.getMedicamentoNombre(),
                d.getCantidadDispensada());
    }
}
