package cl.recetaya.recetas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecetaRequest(
        @NotBlank String pacienteNombre,
        @NotBlank String pacienteRut,
        @NotBlank String medicoNombre,
        String diagnostico,
        @NotEmpty @Valid List<DetalleRecetaRequest> detalles) {
}
