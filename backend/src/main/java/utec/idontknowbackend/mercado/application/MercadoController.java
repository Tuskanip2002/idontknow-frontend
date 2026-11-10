package utec.idontknowbackend.mercado.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.idontknowbackend.mercado.DTO.MercadoDetailDTO;
import utec.idontknowbackend.mercado.DTO.MercadoResponseDTO;
import utec.idontknowbackend.mercado.model.MercadoService;
import utec.idontknowbackend.mercado.model.TraduccionService;

@RestController
@RequestMapping("/mercados")
@RequiredArgsConstructor
public class MercadoController {

    private final MercadoService mercadoService;
    private final TraduccionService traducciones;

    @GetMapping("/{id}/traduccion")
    public ResponseEntity<TraduccionService.Traduccion> traducir(@PathVariable Long id) {
        return ResponseEntity.ok(traducciones.traducir(id));
    }

    @GetMapping
    public ResponseEntity<Page<MercadoResponseDTO>> getAll(
            @RequestParam(defaultValue = "") String categoria,
            @RequestParam(defaultValue = "") String q,
            Pageable pageable) {
        return ResponseEntity.ok(mercadoService.getAll(categoria, q, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MercadoDetailDTO> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(mercadoService.getDetalle(id));
    }
}
