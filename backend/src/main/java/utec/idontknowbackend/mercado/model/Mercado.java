package utec.idontknowbackend.mercado.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.categoria.model.Categoria;
import utec.idontknowbackend.snapshot.model.Snapshot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "mercados", indexes = @Index(name = "idx_polymarket_id", columnList = "polymarketId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mercado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String polymarketId;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String preguntaOriginal; // texto en inglés tal cual viene de Polymarket

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(nullable = false)
    private BigDecimal probabilidadActual;

    @Builder.Default
    private Boolean resuelto = false;

    private LocalDateTime fechaResolucionEstimada;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mercado_categoria",
            joinColumns = @JoinColumn(name = "mercado_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "mercado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Snapshot> snapshots = new HashSet<>();
}
