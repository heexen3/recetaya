package cl.recetaya.auth.dto;

import cl.recetaya.auth.model.Usuario;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        String rol,
        boolean activo) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getRol().name(),
                u.isActivo());
    }
}
