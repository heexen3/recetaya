package cl.recetaya.inventario.dto;

import cl.recetaya.inventario.model.Stock;

public record StockResponse(
        Long id,
        Long medicamentoId,
        String medicamentoNombre,
        Long sucursalId,
        String sucursalNombre,
        int cantidad) {

    public static StockResponse from(Stock s) {
        return new StockResponse(
                s.getId(),
                s.getMedicamento().getId(),
                s.getMedicamento().getNombre(),
                s.getSucursal().getId(),
                s.getSucursal().getNombre(),
                s.getCantidad());
    }
}
