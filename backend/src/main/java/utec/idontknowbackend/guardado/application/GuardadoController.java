package utec.idontknowbackend.guardado.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.idontknowbackend.guardado.DTO.GuardadoRequestDTO;
import utec.idontknowbackend.guardado.DTO.GuardadoResponseDTO;
import utec.idontknowbackend.guardado.model.GuardadoService;

import java.util.List;

@RestController
@RequestMapping("/guardados")
@RequiredArgsConstructor
public class GuardadoController {

    private final GuardadoService guardadoService;

    @PostMapping
    public ResponseEntity<GuardadoResponseDTO> guardar(@Valid @RequestBody GuardadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guardadoService.guardar(dto.getTitularId()));
    }

    @DeleteMapping("/{titularId}")
    public ResponseEntity<Void> quitar(@PathVariable Long titularId) {
        guardadoService.quitar(titularId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<GuardadoResponseDTO>> listar() {
        return ResponseEntity.ok(guardadoService.listar());
    }
}
