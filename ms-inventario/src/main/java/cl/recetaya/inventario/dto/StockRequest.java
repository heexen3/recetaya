package cl.recetaya.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull Long medicamentoId,
        @NotNull Long sucursalId,
        @Min(0) int cantidad) {
}
