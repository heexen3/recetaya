package cl.recetaya.dispensacion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "detalle_dispensaciones")
public class DetalleDispensacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long medicamentoId;

    private String medicamentoNombre;

    @Min(1)
    @Column(nullable = false)
    private int cantidadDispensada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispensacion_id")
    @JsonIgnore
    private Dispensacion dispensacion;
}
