package utec.idontknowbackend.guardado.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.titular.DTO.TitularResponseDTO;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardadoResponseDTO {

    private Long id;
    private TitularResponseDTO titular;
    private LocalDateTime fechaGuardado;
}
