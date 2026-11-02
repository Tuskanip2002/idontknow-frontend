package utec.idontknowbackend.mercado.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.snapshot.infrastructure.SnapshotRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoServiceTest {

    @Mock private MercadoRepository mercadoRepository;
    @Mock private SnapshotRepository snapshotRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper = new ModelMapper();

    private MercadoService mercadoService;

    @BeforeEach
    void setUp() {
        mercadoService = new MercadoService(mercadoRepository, snapshotRepository, modelMapper, eventPublisher);
    }

    @Test
    void actualizarProbabilidad_alCruzarUmbral_publicaEvento() {
        Mercado mercado = Mercado.builder().id(1L).probabilidadActual(new BigDecimal("0.40")).build();

        mercadoService.actualizarProbabilidad(mercado, new BigDecimal("0.55"));

        verify(eventPublisher).publishEvent(any(MercadoUmbralCruzadoEvent.class));
        verify(mercadoRepository).save(mercado);
    }

    @Test
    void actualizarProbabilidad_sinCruzarUmbral_noPublicaEvento() {
        Mercado mercado = Mercado.builder().id(1L).probabilidadActual(new BigDecimal("0.55")).build();

        mercadoService.actualizarProbabilidad(mercado, new BigDecimal("0.60"));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getDetalle_conIdInexistente_lanzaExcepcion() {
        when(mercadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mercadoService.getDetalle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
