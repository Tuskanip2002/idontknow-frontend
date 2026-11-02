package utec.idontknowbackend.snapshot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.mercado.model.Mercado;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"mercado_id", "fecha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mercado_id", nullable = false)
    private Mercado mercado;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(nullable = false)
    private BigDecimal probabilidad;

    @Column(nullable = false)
    private LocalDate fecha;
}
