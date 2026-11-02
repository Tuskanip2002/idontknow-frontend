package utec.idontknowbackend.mercado.DTO;

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
public class MercadoDetailDTO {

    private Long id;
    private String polymarketId;
    private String preguntaOriginal;
    private BigDecimal probabilidadActual;
    private Boolean resuelto;
    private LocalDateTime fechaResolucionEstimada;
    private List<String> categorias;
    private List<SnapshotResponseDTO> historial; // para el gráfico de "cómo cambió"
}
