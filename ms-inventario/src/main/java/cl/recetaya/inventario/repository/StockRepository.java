package cl.recetaya.inventario.repository;

import cl.recetaya.inventario.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByMedicamentoIdAndSucursalId(Long medicamentoId, Long sucursalId);
    List<Stock> findBySucursalId(Long sucursalId);
}
