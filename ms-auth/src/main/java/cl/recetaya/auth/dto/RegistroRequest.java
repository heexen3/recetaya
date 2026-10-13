package cl.recetaya.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String nombreCompleto,
        @NotNull String rol) {
}
