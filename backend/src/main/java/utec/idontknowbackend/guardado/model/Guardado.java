package utec.idontknowbackend.guardado.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.titular.model.Titular;
import utec.idontknowbackend.user.model.Usuario;

import java.time.LocalDateTime;

@Entity
@Table(name = "guardados", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "titular_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guardado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaGuardado = LocalDateTime.now();
}
