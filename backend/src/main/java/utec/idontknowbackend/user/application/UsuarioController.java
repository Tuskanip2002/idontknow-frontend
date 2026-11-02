package utec.idontknowbackend.user.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.idontknowbackend.user.DTO.UsuarioResponseDTO;
import utec.idontknowbackend.user.DTO.UsuarioUpdateRequestDTO;
import utec.idontknowbackend.user.model.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getPerfil() {
        return ResponseEntity.ok(usuarioService.getPerfil());
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> update(@Valid @RequestBody UsuarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.update(dto));
    }

    @PostMapping("/me/categorias/{categoriaId}")
    public ResponseEntity<UsuarioResponseDTO> seguirCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(usuarioService.seguirCategoria(categoriaId));
    }

    @DeleteMapping("/me/categorias/{categoriaId}")
    public ResponseEntity<Void> dejarDeSeguirCategoria(@PathVariable Long categoriaId) {
        usuarioService.dejarDeSeguirCategoria(categoriaId);
        return ResponseEntity.noContent().build();
    }
}
