package utec.idontknowbackend.edicion.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.titular.DTO.TitularResponseDTO;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdicionResponseDTO {

    private Long id;
    private LocalDate fecha;
    private List<TitularResponseDTO> titulares;
}
