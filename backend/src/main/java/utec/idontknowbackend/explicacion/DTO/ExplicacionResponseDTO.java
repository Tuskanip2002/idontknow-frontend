package utec.idontknowbackend.explicacion.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo freemium: todos ven cuántas fuentes hay (el adelanto), pero solo Premium
 * recibe el resumen y las fuentes. Con bloqueado = true, resumen es null y fuentes viene vacía.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplicacionResponseDTO {
    private Long titularId;
    private boolean disponible;          // false si todavía no se generó
    private boolean bloqueado;           // true si el usuario no es Premium
    private int cantidadFuentes;
    private List<String> tiposFuente;    // ej. ["NOTICIA", "COMENTARIO"]
    private String resumen;
    private List<FuenteResponseDTO> fuentes;
    private LocalDateTime generadaEn;
}
