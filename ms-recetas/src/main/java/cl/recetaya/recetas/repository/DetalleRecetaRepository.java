package cl.recetaya.recetas.repository;

import cl.recetaya.recetas.model.DetalleReceta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {
}
