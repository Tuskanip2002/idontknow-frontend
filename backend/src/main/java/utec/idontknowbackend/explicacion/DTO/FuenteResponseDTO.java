package utec.idontknowbackend.explicacion.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuenteResponseDTO {
    private String tipo;          // "NOTICIA" o "COMENTARIO"
    private String titulo;
    private String url;           // null en comentarios
    private String medio;
    private LocalDateTime fecha;
}
