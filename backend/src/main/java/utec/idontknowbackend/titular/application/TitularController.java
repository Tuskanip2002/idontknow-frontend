package utec.idontknowbackend.titular.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.idontknowbackend.titular.DTO.TitularDetailDTO;
import utec.idontknowbackend.titular.model.TitularService;

@RestController
@RequestMapping("/titulares")
@RequiredArgsConstructor
public class TitularController {

    private final TitularService titularService;

    @GetMapping("/{id}")
    public ResponseEntity<TitularDetailDTO> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(titularService.getDetalle(id));
    }
}
