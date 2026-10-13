package cl.recetaya.auth.dto;

public record AuthResponse(
        String token,
        String username,
        String rol) {
}
