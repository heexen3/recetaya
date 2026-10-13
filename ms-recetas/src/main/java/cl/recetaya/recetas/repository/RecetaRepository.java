package cl.recetaya.recetas.repository;

import cl.recetaya.recetas.model.Receta;
import cl.recetaya.recetas.model.EstadoReceta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByPacienteRut(String pacienteRut);
    List<Receta> findByEstado(EstadoReceta estado);
}
