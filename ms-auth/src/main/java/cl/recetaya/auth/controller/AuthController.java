package cl.recetaya.auth.controller;

import cl.recetaya.auth.dto.*;
import cl.recetaya.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return service.listarUsuarios();
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return service.obtenerUsuario(id);
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        service.eliminarUsuario(id);
    }
}
