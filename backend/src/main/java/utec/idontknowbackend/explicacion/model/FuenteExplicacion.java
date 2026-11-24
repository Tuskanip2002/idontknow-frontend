package utec.idontknowbackend.explicacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Una fuente citada en la explicación: una noticia o un comentario. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuenteExplicacion {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFuente tipo;

    @Column(nullable = false, length = 300)
    private String titulo;     // título de la noticia o texto del comentario

    @Column(length = 1000)
    private String url;        // null en los comentarios

    @Column(length = 200)
    private String medio;      // dominio del medio, o "Comentario en Polymarket"

    private LocalDateTime fecha;
}
