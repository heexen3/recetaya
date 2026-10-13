package cl.recetaya.auth.service;

import cl.recetaya.auth.dto.*;
import cl.recetaya.auth.exception.CredencialesInvalidasException;
import cl.recetaya.auth.exception.RecursoNoEncontradoException;
import cl.recetaya.auth.exception.UsuarioDuplicadoException;
import cl.recetaya.auth.model.Rol;
import cl.recetaya.auth.model.Usuario;
import cl.recetaya.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest req) {
        if (repo.existsByUsername(req.username())) {
            throw new UsuarioDuplicadoException("El usuario '" + req.username() + "' ya existe");
        }

        Usuario u = new Usuario();
        u.setUsername(req.username());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setNombreCompleto(req.nombreCompleto());
        u.setRol(Rol.valueOf(req.rol().toUpperCase()));
        repo.save(u);

        String token = jwtService.generarToken(u.getUsername(), u.getRol().name());
        return new AuthResponse(token, u.getUsername(), u.getRol().name());
    }

    public AuthResponse login(LoginRequest req) {
        Usuario u = repo.findByUsername(req.username())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        if (!u.isActivo()) {
            throw new CredencialesInvalidasException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        String token = jwtService.generarToken(u.getUsername(), u.getRol().name());
        return new AuthResponse(token, u.getUsername(), u.getRol().name());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return repo.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Long id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        repo.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario " + id + " no existe"));
    }
}
