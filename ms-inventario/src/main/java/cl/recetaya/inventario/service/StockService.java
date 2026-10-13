package cl.recetaya.inventario.service;

import cl.recetaya.inventario.dto.ReservaRequest;
import cl.recetaya.inventario.dto.StockRequest;
import cl.recetaya.inventario.dto.StockResponse;
import cl.recetaya.inventario.exception.RecursoNoEncontradoException;
import cl.recetaya.inventario.exception.StockInsuficienteException;
import cl.recetaya.inventario.model.Stock;
import cl.recetaya.inventario.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepo;
    private final MedicamentoService medicamentos;
    private final SucursalService sucursales;

    public StockService(StockRepository stockRepo, MedicamentoService medicamentos, SucursalService sucursales) {
        this.stockRepo = stockRepo;
        this.medicamentos = medicamentos;
        this.sucursales = sucursales;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> listarPorSucursal(Long sucursalId) {
        sucursales.obtener(sucursalId);
        return stockRepo.findBySucursalId(sucursalId).stream().map(StockResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StockResponse consultar(Long medicamentoId, Long sucursalId) {
        return StockResponse.from(buscar(medicamentoId, sucursalId));
    }

    /** Crea o fija la cantidad de stock de un medicamento en una sucursal. */
    @Transactional
    public StockResponse fijar(StockRequest req) {
        Stock stock = stockRepo.findByMedicamentoIdAndSucursalId(req.medicamentoId(), req.sucursalId())
                .orElseGet(() -> {
                    Stock nuevo = new Stock();
                    nuevo.setMedicamento(medicamentos.obtener(req.medicamentoId()));
                    nuevo.setSucursal(sucursales.obtener(req.sucursalId()));
                    return nuevo;
                });
        stock.setCantidad(req.cantidad());
        return StockResponse.from(stockRepo.save(stock));
    }

    /** Descuenta stock. Lo usará el consumidor del evento de receta y Dispensación. */
    @Transactional
    public StockResponse reservar(ReservaRequest req) {
        Stock stock = buscar(req.medicamentoId(), req.sucursalId());
        if (stock.getCantidad() < req.cantidad()) {
            throw new StockInsuficienteException("Stock insuficiente: disponible "
                    + stock.getCantidad() + ", solicitado " + req.cantidad());
        }
        stock.setCantidad(stock.getCantidad() - req.cantidad());
        return StockResponse.from(stockRepo.save(stock));
    }

    private Stock buscar(Long medicamentoId, Long sucursalId) {
        return stockRepo.findByMedicamentoIdAndSucursalId(medicamentoId, sucursalId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay stock del medicamento " + medicamentoId + " en la sucursal " + sucursalId));
    }
}
