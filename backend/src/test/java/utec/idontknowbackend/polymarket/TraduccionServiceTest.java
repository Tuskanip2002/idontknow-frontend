package utec.idontknowbackend.polymarket;

import org.junit.jupiter.api.Test;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.mercado.model.TraduccionService;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.exceptions.ExternalServiceException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraduccionServiceTest {
    @Test
    void reutilizaLaTraduccionPersistida() {
        var repo = mock(MercadoRepository.class);
        var groq = mock(GroqTraduccionClient.class);
        var mercado = Mercado.builder().preguntaOriginal("Will it happen?").preguntaEspanol("¿Ocurrirá?").build();
        when(repo.findById(1L)).thenReturn(Optional.of(mercado));
        assertEquals("¿Ocurrirá?", new TraduccionService(repo, groq).traducir(1L).espanol());
        verifyNoInteractions(groq);
    }

    @Test
    void noGuardaElOriginalComoTraduccionSiElProveedorFalla() {
        var repo = mock(MercadoRepository.class);
        var groq = mock(GroqTraduccionClient.class);
        var mercado = Mercado.builder().preguntaOriginal("Will it happen?").build();
        when(repo.findById(1L)).thenReturn(Optional.of(mercado));
        when(groq.traducir("Will it happen?", null)).thenReturn("Will it happen?");
        assertThrows(ExternalServiceException.class, () -> new TraduccionService(repo, groq).traducir(1L));
        verify(repo, never()).save(any());
    }
}
