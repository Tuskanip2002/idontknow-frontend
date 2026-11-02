package utec.idontknowbackend.categoria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.user.model.Usuario;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // POLITICA, ECONOMIA, DEPORTES, TECNOLOGIA, etc.

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Mercado> mercados = new HashSet<>();

    @ManyToMany(mappedBy = "categoriasSeguidas", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Usuario> seguidores = new HashSet<>();
}
