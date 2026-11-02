package utec.idontknowbackend.guardado.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.guardado.infrastructure.GuardadoRepository;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Usuario;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardadoServiceTest {

    @Mock private GuardadoRepository guardadoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TitularRepository titularRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    private GuardadoService guardadoService;

    @BeforeEach
    void setUp() {
        guardadoService = new GuardadoService(guardadoRepository, usuarioRepository, titularRepository, modelMapper);
        Usuario usuarioAutenticado = Usuario.builder().id(1L).email("joseph@test.com").build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("joseph@test.com", null));
        when(usuarioRepository.findByEmail("joseph@test.com")).thenReturn(Optional.of(usuarioAutenticado));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void guardar_titularYaGuardado_lanzaExcepcion() {
        Titular titular = Titular.builder().id(10L).build();
        when(titularRepository.findById(10L)).thenReturn(Optional.of(titular));
        when(guardadoRepository.findByUsuarioIdAndTitularId(1L, 10L))
                .thenReturn(Optional.of(new Guardado()));

        assertThatThrownBy(() -> guardadoService.guardar(10L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void guardar_exitoso_loGuarda() {
        Titular titular = Titular.builder().id(10L).textoEspanol("Test").build();
        when(titularRepository.findById(10L)).thenReturn(Optional.of(titular));
        when(guardadoRepository.findByUsuarioIdAndTitularId(1L, 10L)).thenReturn(Optional.empty());
        when(guardadoRepository.save(any(Guardado.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> guardadoService.guardar(10L)).doesNotThrowAnyException();
        verify(guardadoRepository).save(any(Guardado.class));
    }

    @Test
    void quitar_noExiste_lanzaExcepcion() {
        when(guardadoRepository.findByUsuarioIdAndTitularId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guardadoService.quitar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
