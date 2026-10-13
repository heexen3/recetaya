package cl.recetaya.dispensacion.dto;

import cl.recetaya.dispensacion.model.Dispensacion;

import java.time.LocalDateTime;
import java.util.List;

public record DispensacionResponse(
        Long id,
        Long recetaId,
        Long sucursalId,
        String farmaceuticoNombre,
        LocalDateTime fechaDispensacion,
        List<DetalleDispensacionResponse> detalles) {

    public static DispensacionResponse from(Dispensacion d) {
        return new DispensacionResponse(
                d.getId(),
                d.getRecetaId(),
                d.getSucursalId(),
                d.getFarmaceuticoNombre(),
                d.getFechaDispensacion(),
                d.getDetalles().stream().map(DetalleDispensacionResponse::from).toList());
    }
}
