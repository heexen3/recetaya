package cl.recetaya.inventario.controller;

import cl.recetaya.inventario.model.Medicamento;
import cl.recetaya.inventario.service.MedicamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentoController {

    private final MedicamentoService service;

    public MedicamentoController(MedicamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Medicamento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Medicamento obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medicamento crear(@Valid @RequestBody Medicamento m) {
        return service.crear(m);
    }

    @PutMapping("/{id}")
    public Medicamento actualizar(@PathVariable Long id, @Valid @RequestBody Medicamento m) {
        return service.actualizar(id, m);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
