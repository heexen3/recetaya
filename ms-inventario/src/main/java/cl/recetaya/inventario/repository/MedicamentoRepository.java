package cl.recetaya.inventario.repository;

import cl.recetaya.inventario.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    boolean existsByCodigo(String codigo);
}
