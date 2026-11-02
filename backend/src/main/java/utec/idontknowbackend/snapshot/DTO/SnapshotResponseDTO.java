package utec.idontknowbackend.snapshot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotResponseDTO {

    private LocalDate fecha;
    private BigDecimal probabilidad;
}
