package utec.idontknowbackend.edicion.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdicionSummaryDTO {

    private Long id;
    private LocalDate fecha;
    private Integer cantidadTitulares; // para el listado del "Archivo" sin traer todo el detalle
}
