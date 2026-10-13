package cl.recetaya.dispensacion.controller;

import cl.recetaya.dispensacion.dto.DispensacionRequest;
import cl.recetaya.dispensacion.dto.DispensacionResponse;
import cl.recetaya.dispensacion.service.DispensacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispensaciones")
public class DispensacionController {

    private final DispensacionService service;

    public DispensacionController(DispensacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<DispensacionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public DispensacionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/receta/{recetaId}")
    public List<DispensacionResponse> buscarPorReceta(@PathVariable Long recetaId) {
        return service.buscarPorReceta(recetaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DispensacionResponse dispensar(@Valid @RequestBody DispensacionRequest req) {
        return service.dispensar(req);
    }
}
