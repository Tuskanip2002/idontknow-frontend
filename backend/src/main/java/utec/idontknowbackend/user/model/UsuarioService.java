package utec.idontknowbackend.user.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.categoria.model.Categoria;
import utec.idontknowbackend.exceptions.InvalidOperationException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.user.DTO.UsuarioResponseDTO;
import utec.idontknowbackend.user.DTO.UsuarioUpdateRequestDTO;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;

import java.time.LocalDate;
import java.time.ZoneId;

@org.springframework.transaction.annotation.Transactional
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // la suscripción es simulada (sin pasarela de pago); se puede apagar en producción
    @Value("${suscripcion.demo.habilitada:true}")
    private boolean suscripcionDemoHabilitada;

    @Value("${suscripcion.demo.dias:30}")
    private int diasSuscripcionDemo;

    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public UsuarioResponseDTO getPerfil() {
        return toResponseDTO(getUsuarioAutenticado());
    }

    public UsuarioResponseDTO update(UsuarioUpdateRequestDTO dto) {
        Usuario usuario = getUsuarioAutenticado();

        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        if (dto.getPassword() != null) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO seguirCategoria(Long categoriaId) {
        Usuario usuario = getUsuarioAutenticado();
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        usuario.getCategoriasSeguidas().add(categoria);
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO dejarDeSeguirCategoria(Long categoriaId) {
        Usuario usuario = getUsuarioAutenticado();
        boolean laSeguia = usuario.getCategoriasSeguidas().removeIf(c -> c.getId().equals(categoriaId));
        if (!laSeguia) {
            throw new InvalidOperationException("No sigues esa categoría");
        }
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO activarSuscripcionDemo() {
        if (!suscripcionDemoHabilitada) {
            throw new InvalidOperationException("La suscripción de demostración no está disponible");
        }
        Usuario usuario = getUsuarioAutenticado();
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        // si ya es premium, se extiende desde su fecha de vencimiento en vez de perder días
        LocalDate desde = usuario.esPremium() ? usuario.getPremiumHasta() : hoy;
        usuario.setPlan(Plan.PREMIUM);
        usuario.setPremiumHasta(desde.plusDays(diasSuscripcionDemo));
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO cancelarSuscripcion() {
        Usuario usuario = getUsuarioAutenticado();
        usuario.setPlan(Plan.FREE);
        usuario.setPremiumHasta(null);
        return toResponseDTO(usuarioRepository.save(usuario));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = modelMapper.map(usuario, UsuarioResponseDTO.class);
        dto.setCategoriasSeguidas(
                usuario.getCategoriasSeguidas().stream()
                        .map(Categoria::getNombre)
                        .toList()
        );
        boolean premium = usuario.esPremium();
        dto.setPlan(premium ? Plan.PREMIUM.name() : Plan.FREE.name());
        dto.setPremiumHasta(premium ? usuario.getPremiumHasta() : null);
        return dto;
    }
}
