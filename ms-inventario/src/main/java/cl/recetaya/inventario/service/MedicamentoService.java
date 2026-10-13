package cl.recetaya.inventario.service;

import cl.recetaya.inventario.exception.RecursoNoEncontradoException;
import cl.recetaya.inventario.model.Medicamento;
import cl.recetaya.inventario.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicamentoService {

    private final MedicamentoRepository repo;

    public MedicamentoService(MedicamentoRepository repo) {
        this.repo = repo;
    }

    public List<Medicamento> listar() {
        return repo.findAll();
    }

    public Medicamento obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento " + id + " no existe"));
    }

    public Medicamento crear(Medicamento m) {
        m.setId(null);
        return repo.save(m);
    }

    public Medicamento actualizar(Long id, Medicamento datos) {
        Medicamento m = obtener(id);
        m.setCodigo(datos.getCodigo());
        m.setNombre(datos.getNombre());
        m.setPrincipioActivo(datos.getPrincipioActivo());
        m.setRequiereReceta(datos.isRequiereReceta());
        return repo.save(m);
    }

    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }
}
