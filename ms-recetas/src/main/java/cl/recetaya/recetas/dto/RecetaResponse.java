package cl.recetaya.recetas.dto;

import cl.recetaya.recetas.model.Receta;

import java.time.LocalDateTime;
import java.util.List;

public record RecetaResponse(
        Long id,
        String pacienteNombre,
        String pacienteRut,
        String medicoNombre,
        String estado,
        LocalDateTime fechaEmision,
        String diagnostico,
        List<DetalleRecetaResponse> detalles) {

    public static RecetaResponse from(Receta r) {
        return new RecetaResponse(
                r.getId(),
                r.getPacienteNombre(),
                r.getPacienteRut(),
                r.getMedicoNombre(),
                r.getEstado().name(),
                r.getFechaEmision(),
                r.getDiagnostico(),
                r.getDetalles().stream().map(DetalleRecetaResponse::from).toList());
    }
}
