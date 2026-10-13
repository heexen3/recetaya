package cl.recetaya.dispensacion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DetalleDispensacionRequest(
        @NotNull Long medicamentoId,
        String medicamentoNombre,
        @Min(1) int cantidadDispensada) {
}
