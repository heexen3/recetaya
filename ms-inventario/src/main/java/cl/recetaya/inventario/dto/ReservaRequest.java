package cl.recetaya.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull Long medicamentoId,
        @NotNull Long sucursalId,
        @Min(1) int cantidad) {
}
