package utec.idontknowbackend.titular.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.edicion.model.Edicion;
import utec.idontknowbackend.mercado.model.Mercado;

import java.math.BigDecimal;

@Entity
@Table(name = "titulares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Titular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "edicion_id", nullable = false)
    private Edicion edicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercado_id", nullable = false)
    private Mercado mercado;

    @NotBlank
    @Column(nullable = false, length = 300)
    private String textoEspanol;

    @Column(nullable = false)
    private BigDecimal probabilidadHoy;

    @Column(nullable = false)
    private BigDecimal cambioDesdeAyer;

    @Column(nullable = false)
    private Double puntaje; // resultado del ScoreCalculator, decide si entra a portada
}

