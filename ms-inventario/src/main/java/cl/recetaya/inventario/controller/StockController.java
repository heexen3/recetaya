package cl.recetaya.inventario.controller;

import cl.recetaya.inventario.dto.ReservaRequest;
import cl.recetaya.inventario.dto.StockRequest;
import cl.recetaya.inventario.dto.StockResponse;
import cl.recetaya.inventario.service.StockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public List<StockResponse> listarPorSucursal(@RequestParam Long sucursalId) {
        return service.listarPorSucursal(sucursalId);
    }

    @GetMapping("/consulta")
    public StockResponse consultar(@RequestParam Long medicamentoId, @RequestParam Long sucursalId) {
        return service.consultar(medicamentoId, sucursalId);
    }

    @PutMapping
    public StockResponse fijar(@Valid @RequestBody StockRequest req) {
        return service.fijar(req);
    }

    @PutMapping("/reservar")
    public StockResponse reservar(@Valid @RequestBody ReservaRequest req) {
        return service.reservar(req);
    }
}
