package cl.recetaya.inventario.controller;

import cl.recetaya.inventario.model.Sucursal;
import cl.recetaya.inventario.service.SucursalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    private final SucursalService service;

    public SucursalController(SucursalService service) {
        this.service = service;
    }

    @GetMapping
    public List<Sucursal> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Sucursal obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sucursal crear(@Valid @RequestBody Sucursal s) {
        return service.crear(s);
    }

    @PutMapping("/{id}")
    public Sucursal actualizar(@PathVariable Long id, @Valid @RequestBody Sucursal s) {
        return service.actualizar(id, s);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
