package utec.idontknowbackend.mercado.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MercadoResponseDTO {

    private Long id;
    private String preguntaOriginal;
    private BigDecimal probabilidadActual;
    private Boolean resuelto;
    private List<String> categorias;
}
