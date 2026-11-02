package utec.idontknowbackend.titular.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utec.idontknowbackend.snapshot.DTO.SnapshotResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitularDetailDTO {

    private Long id;
    private String textoEspanol;
    private BigDecimal probabilidadHoy;
    private BigDecimal cambioDesdeAyer;
    private LocalDateTime fechaResolucionEstimada;
    private List<SnapshotResponseDTO> historial; // el gráfico del detalle del titular
}
