package utec.idontknowbackend.guardado.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.guardado.DTO.GuardadoResponseDTO;
import utec.idontknowbackend.guardado.infrastructure.GuardadoRepository;
import utec.idontknowbackend.titular.DTO.TitularResponseDTO;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Usuario;

import java.util.List;

@org.springframework.transaction.annotation.Transactional
@Service
@RequiredArgsConstructor
public class GuardadoService {

    private final GuardadoRepository guardadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TitularRepository titularRepository;
    private final ModelMapper modelMapper;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email).orElseThrow();
    }

    public GuardadoResponseDTO guardar(Long titularId) {
        Usuario usuario = getUsuarioAutenticado();
        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new ResourceNotFoundException("Titular no encontrado"));

        guardadoRepository.findByUsuarioIdAndTitularId(usuario.getId(), titularId)
                .ifPresent(g -> { throw new DuplicateResourceException("Ya guardaste este titular"); });

        Guardado guardado = Guardado.builder()
                .usuario(usuario)
                .titular(titular)
                .build();

        return toResponseDTO(guardadoRepository.save(guardado));
    }

    public void quitar(Long titularId) {
        Usuario usuario = getUsuarioAutenticado();
        Guardado guardado = guardadoRepository.findByUsuarioIdAndTitularId(usuario.getId(), titularId)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes guardado ese titular"));

        guardadoRepository.delete(guardado);
    }

    public List<GuardadoResponseDTO> listar() {
        Usuario usuario = getUsuarioAutenticado();
        return guardadoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private GuardadoResponseDTO toResponseDTO(Guardado guardado) {
        GuardadoResponseDTO dto = modelMapper.map(guardado, GuardadoResponseDTO.class);
        dto.setTitular(modelMapper.map(guardado.getTitular(), TitularResponseDTO.class));
        return dto;
    }
}
