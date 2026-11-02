package utec.idontknowbackend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.InvalidCredentialsException;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Role;
import utec.idontknowbackend.user.model.Usuario;
import utec.idontknowbackend.user.model.UsuarioRegistradoEvent;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService, eventPublisher);
    }

    @Test
    void register_conEmailYaRegistrado_lanzaExcepcion() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Joseph", "joseph@test.com", "Password123");
        when(usuarioRepository.existsByEmail("joseph@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void register_exitoso_devuelveTokenYPublicaEvento() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Joseph", "joseph@test.com", "Password123");
        when(usuarioRepository.existsByEmail("joseph@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken("joseph@test.com")).thenReturn("fake-jwt");

        AuthResponseDTO resultado = authService.register(dto);

        assertThat(resultado.getToken()).isEqualTo("fake-jwt");
        assertThat(resultado.getRole()).isEqualTo("USER");
        verify(eventPublisher).publishEvent(any(UsuarioRegistradoEvent.class));
    }

    @Test
    void login_conPasswordIncorrecta_lanzaExcepcion() {
        LoginRequestDTO dto = new LoginRequestDTO("joseph@test.com", "wrong");
        Usuario usuario = Usuario.builder().email("joseph@test.com").password("hash").role(Role.USER).build();
        when(usuarioRepository.findByEmail("joseph@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_exitoso_devuelveToken() {
        LoginRequestDTO dto = new LoginRequestDTO("joseph@test.com", "Password123");
        Usuario usuario = Usuario.builder().email("joseph@test.com").password("hash").role(Role.USER).nombre("Joseph").build();
        when(usuarioRepository.findByEmail("joseph@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(jwtService.generateToken("joseph@test.com")).thenReturn("fake-jwt");

        AuthResponseDTO resultado = authService.login(dto);

        assertThat(resultado.getToken()).isEqualTo("fake-jwt");
    }
}
