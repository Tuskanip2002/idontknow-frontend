package utec.idontknowbackend.explicacion.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import utec.idontknowbackend.explicacion.DTO.ExplicacionResponseDTO;
import utec.idontknowbackend.explicacion.model.ExplicacionService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExplicacionController {

    private final ExplicacionService explicacionService;

    // público: sin Premium devuelve solo el adelanto (cantidad y tipo de fuentes)
    @GetMapping("/titulares/{id}/explicacion")
    public ResponseEntity<ExplicacionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(explicacionService.obtener(id));
    }

    // solo ADMIN (ver SecurityConfig): útil justo después de desplegar
    @PostMapping("/explicaciones/regenerar")
    public ResponseEntity<Map<String, String>> regenerar() {
        explicacionService.regenerarHoy();
        return ResponseEntity.accepted().body(Map.of(
                "message", "Generando las explicaciones de la portada de hoy. Puede tardar un par de minutos."));
    }
}
