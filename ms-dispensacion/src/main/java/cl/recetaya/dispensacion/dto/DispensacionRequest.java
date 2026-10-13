package cl.recetaya.dispensacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record DispensacionRequest(
        @NotNull Long recetaId,
        @NotNull Long sucursalId,
        @NotBlank String farmaceuticoNombre,
        @NotEmpty @Valid List<DetalleDispensacionRequest> detalles) {
}
