package utec.idontknowbackend.edicion.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.idontknowbackend.edicion.DTO.EdicionResponseDTO;
import utec.idontknowbackend.edicion.DTO.EdicionSummaryDTO;
import utec.idontknowbackend.edicion.model.EdicionService;

import java.time.LocalDate;

@RestController
@RequestMapping("/ediciones")
@RequiredArgsConstructor
public class EdicionController {

    private final EdicionService edicionService;

    @GetMapping("/hoy")
    public ResponseEntity<EdicionResponseDTO> getHoy() {
        return ResponseEntity.ok(edicionService.getDeHoy());
    }

    @GetMapping("/{fecha}")
    public ResponseEntity<EdicionResponseDTO> getPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(edicionService.getPorFecha(fecha));
    }

    @GetMapping
    public ResponseEntity<Page<EdicionSummaryDTO>> listArchivo(Pageable pageable) {
        return ResponseEntity.ok(edicionService.listArchivo(pageable));
    }
}
