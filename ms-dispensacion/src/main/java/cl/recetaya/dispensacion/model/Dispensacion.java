package cl.recetaya.dispensacion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "dispensaciones")
public class Dispensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long recetaId;

    @NotNull
    @Column(nullable = false)
    private Long sucursalId;

    private String farmaceuticoNombre;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaDispensacion = LocalDateTime.now();

    @OneToMany(mappedBy = "dispensacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDispensacion> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleDispensacion d) {
        detalles.add(d);
        d.setDispensacion(this);
    }
}
