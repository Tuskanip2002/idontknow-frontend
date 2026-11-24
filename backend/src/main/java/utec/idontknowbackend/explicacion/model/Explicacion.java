package utec.idontknowbackend.explicacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.titular.model.Titular;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** "¿Por qué este número?": resumen con fuentes citadas de un titular de la portada. */
@Entity
@Table(name = "explicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Explicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titular_id", nullable = false, unique = true)
    private Titular titular;

    @Column(length = 2000)
    private String resumen;

    // las fuentes se guardan en su propia tabla; @OrderColumn mantiene la numeración [1], [2]...
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "explicacion_fuentes", joinColumns = @JoinColumn(name = "explicacion_id"))
    @OrderColumn(name = "posicion")
    @Builder.Default
    private List<FuenteExplicacion> fuentes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime generadaEn;
}
