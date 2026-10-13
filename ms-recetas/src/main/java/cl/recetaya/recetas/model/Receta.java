package cl.recetaya.recetas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String pacienteNombre;

    @NotBlank
    @Column(nullable = false)
    private String pacienteRut;

    @NotBlank
    @Column(nullable = false)
    private String medicoNombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReceta estado = EstadoReceta.EMITIDA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    private String diagnostico;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReceta> detalles = new ArrayList<>();

    /** Helper para mantener la relación bidireccional. */
    public void agregarDetalle(DetalleReceta d) {
        detalles.add(d);
        d.setReceta(this);
    }
}
