package utec.idontknowbackend.titular.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitularResponseDTO {

    private Long id;
    private String textoEspanol;
    private BigDecimal probabilidadHoy;
    private BigDecimal cambioDesdeAyer;
    private Long mercadoId;
}
