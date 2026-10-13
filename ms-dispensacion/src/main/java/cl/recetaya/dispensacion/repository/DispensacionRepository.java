package cl.recetaya.dispensacion.repository;

import cl.recetaya.dispensacion.model.Dispensacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispensacionRepository extends JpaRepository<Dispensacion, Long> {
    List<Dispensacion> findByRecetaId(Long recetaId);
    Optional<Dispensacion> findFirstByRecetaId(Long recetaId);
}
