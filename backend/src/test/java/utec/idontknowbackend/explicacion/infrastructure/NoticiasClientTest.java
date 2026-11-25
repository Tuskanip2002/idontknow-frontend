package utec.idontknowbackend.explicacion.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticiasClientTest {
    @Mock private GdeltClient gdelt;
    @Mock private NoticiasRssClient rss;

    @Test
    void usaGdeltCuandoDevuelveNoticiasSinConsultarElRespaldo() {
        var fuentes = List.of(FuenteExplicacion.builder().tipo(TipoFuente.NOTICIA).titulo("Noticia").build());
        when(gdelt.buscarNoticias("Netanyahu", 4)).thenReturn(fuentes);
        assertEquals(fuentes, new NoticiasClient(gdelt, rss).buscarNoticias("Netanyahu", 4));
        verifyNoInteractions(rss);
    }

    @Test
    void recuperaNoticiasDelRespaldoCuandoGdeltNoLasDevuelve() {
        var fuentes = List.of(FuenteExplicacion.builder().tipo(TipoFuente.NOTICIA).titulo("Noticia RSS").build());
        when(rss.buscarNoticias("Netanyahu", 4)).thenReturn(fuentes);
        assertEquals(fuentes, new NoticiasClient(gdelt, rss).buscarNoticias("Netanyahu", 4));
        verify(gdelt).buscarNoticias("Netanyahu", 4);
    }

    @Test
    void sinResultadosEnAmbosDevuelveVacioParaConservarLasFuentesPrevias() {
        assertTrue(new NoticiasClient(gdelt, rss).buscarNoticias("Netanyahu", 4).isEmpty());
        verify(rss).buscarNoticias("Netanyahu", 4);
    }

    @Test
    void noConsultaProveedoresSinPalabrasClaveOLimiteValido() {
        var client = new NoticiasClient(gdelt, rss);
        assertTrue(client.buscarNoticias("", 4).isEmpty());
        assertTrue(client.buscarNoticias(null, 4).isEmpty());
        assertTrue(client.buscarNoticias("Netanyahu", 0).isEmpty());
        verifyNoInteractions(gdelt, rss);
    }
}
