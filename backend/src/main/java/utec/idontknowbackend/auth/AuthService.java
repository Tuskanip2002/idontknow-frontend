package utec.idontknowbackend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.InvalidCredentialsException;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Role;
import utec.idontknowbackend.user.model.Usuario;
import utec.idontknowbackend.user.model.UsuarioRegistradoEvent;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, saved));

        String token = jwtService.generateToken(saved.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .nombre(saved.getNombre())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        }

        String token = jwtService.generateToken(usuario.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .role(usuario.getRole().name())
                .build();
    }
}
