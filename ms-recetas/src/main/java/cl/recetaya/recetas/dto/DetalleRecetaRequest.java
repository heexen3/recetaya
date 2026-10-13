package cl.recetaya.recetas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleRecetaRequest(
        @NotNull Long medicamentoId,
        String medicamentoNombre,
        @Min(1) int cantidad,
        String posologia) {
}
