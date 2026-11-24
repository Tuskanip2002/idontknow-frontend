package utec.idontknowbackend.user.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private String role;
    private LocalDateTime fechaRegistro;
    private List<String> categoriasSeguidas;
    private String plan;            // "FREE" o "PREMIUM" (ya considera si venció)
    private LocalDate premiumHasta; // null si el plan es FREE
}
