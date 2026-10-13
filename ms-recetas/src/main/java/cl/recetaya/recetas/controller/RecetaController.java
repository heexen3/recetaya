package cl.recetaya.recetas.controller;

import cl.recetaya.recetas.dto.RecetaRequest;
import cl.recetaya.recetas.dto.RecetaResponse;
import cl.recetaya.recetas.model.EstadoReceta;
import cl.recetaya.recetas.service.RecetaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recetas")
public class RecetaController {

    private final RecetaService service;

    public RecetaController(RecetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecetaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RecetaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/paciente")
    public List<RecetaResponse> buscarPorPaciente(@RequestParam String rut) {
        return service.buscarPorPaciente(rut);
    }

    @GetMapping("/estado")
    public List<RecetaResponse> buscarPorEstado(@RequestParam EstadoReceta estado) {
        return service.buscarPorEstado(estado);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecetaResponse emitir(@Valid @RequestBody RecetaRequest req) {
        return service.emitir(req);
    }

    @PatchMapping("/{id}/dispensar")
    public RecetaResponse dispensar(@PathVariable Long id) {
        return service.marcarDispensada(id);
    }

    @PatchMapping("/{id}/anular")
    public RecetaResponse anular(@PathVariable Long id) {
        return service.anular(id);
    }
}
