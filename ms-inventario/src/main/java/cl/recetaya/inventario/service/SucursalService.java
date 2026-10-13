package cl.recetaya.inventario.service;

import cl.recetaya.inventario.exception.RecursoNoEncontradoException;
import cl.recetaya.inventario.model.Sucursal;
import cl.recetaya.inventario.repository.SucursalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalService {

    private final SucursalRepository repo;

    public SucursalService(SucursalRepository repo) {
        this.repo = repo;
    }

    public List<Sucursal> listar() {
        return repo.findAll();
    }

    public Sucursal obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal " + id + " no existe"));
    }

    public Sucursal crear(Sucursal s) {
        s.setId(null);
        return repo.save(s);
    }

    public Sucursal actualizar(Long id, Sucursal datos) {
        Sucursal s = obtener(id);
        s.setNombre(datos.getNombre());
        s.setDireccion(datos.getDireccion());
        return repo.save(s);
    }

    public void eliminar(Long id) {
        repo.delete(obtener(id));
    }
}
