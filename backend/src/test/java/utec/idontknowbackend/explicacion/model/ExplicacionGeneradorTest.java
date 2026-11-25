package utec.idontknowbackend.explicacion.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.explicacion.infrastructure.ExplicacionRepository;
import utec.idontknowbackend.explicacion.infrastructure.GdeltClient;
import utec.idontknowbackend.explicacion.infrastructure.PolymarketComentariosClient;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.polymarket.GroqTraduccionClient;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplicacionGeneradorTest {
    @Mock private TitularRepository titulares;
    @Mock private EdicionRepository ediciones;
    @Mock private ExplicacionRepository explicaciones;
    @Mock private GdeltClient gdelt;
    @Mock private PolymarketComentariosClient comentarios;
    @Mock private GroqTraduccionClient groq;
    private ExplicacionGenerador generador;
    private Titular titular;

    @BeforeEach
    void preparar() {
        generador = new ExplicacionGenerador(titulares, ediciones, explicaciones, gdelt, comentarios, groq);
        titular = Titular.builder().id(204L).mercado(Mercado.builder()
                .polymarketId("682705").preguntaOriginal("Will Netanyahu win?").build()).build();
    }

    private FuenteExplicacion fuente(TipoFuente tipo, String titulo) {
        return FuenteExplicacion.builder().tipo(tipo).titulo(titulo).build();
    }

    private Explicacion anterior(FuenteExplicacion... fuentes) {
        var explicacion = Explicacion.builder().titular(titular).resumen("Resumen previo [1]")
                .generadaEn(LocalDateTime.of(2026, 11, 24, 22, 0))
                .fuentes(new ArrayList<>(List.of(fuentes))).build();
        when(explicaciones.findByTitularId(204L)).thenReturn(Optional.of(explicacion));
        return explicacion;
    }

    @Test
    void unaConsultaVaciaNoBorraFuentesResumenNiFechaPrevios() {
        var noticia = fuente(TipoFuente.NOTICIA, "Noticia guardada");
        var previa = anterior(noticia);
        LocalDateTime fecha = previa.getGeneradaEn();
        generador.generar(titular);
        assertEquals(List.of(noticia), previa.getFuentes());
        assertEquals("Resumen previo [1]", previa.getResumen());
        assertEquals(fecha, previa.getGeneradaEn());
        verify(explicaciones, never()).save(any());
        verifyNoInteractions(groq);
    }

    @Test
    void conservaNoticiasPreviasSiSoloLleganComentariosNuevos() {
        var noticia = fuente(TipoFuente.NOTICIA, "Noticia guardada");
        var comentario = fuente(TipoFuente.COMENTARIO, "Comentario nuevo");
        var previa = anterior(noticia);
        when(comentarios.obtenerComentarios("682705", 3)).thenReturn(List.of(comentario));
        when(groq.resumirMovimiento(anyString(), any(), any(), anyList())).thenReturn("Nuevo resumen [1] [2]");
        generador.generar(titular);
        assertEquals(List.of(comentario, noticia), previa.getFuentes());
        assertEquals("Nuevo resumen [1] [2]", previa.getResumen());
        verify(groq).resumirMovimiento(anyString(), any(), any(), argThat(lista -> lista.size() == 2));
        verify(explicaciones).save(previa);
    }

    @Test
    void reemplazaLasFuentesDelProveedorQueSiDevuelveResultados() {
        var antigua = fuente(TipoFuente.NOTICIA, "Noticia antigua");
        var nueva = fuente(TipoFuente.NOTICIA, "Noticia nueva");
        var previa = anterior(antigua);
        when(gdelt.buscarNoticias(anyString(), eq(4))).thenReturn(List.of(nueva));
        generador.generar(titular);
        assertEquals(List.of(nueva), previa.getFuentes());
        verify(explicaciones).save(previa);
    }

    @Test
    void sinHistorialNiFuentesGuardaElMensajeDeIndisponibilidad() {
        when(explicaciones.findByTitularId(204L)).thenReturn(Optional.empty());
        generador.generar(titular);
        verify(explicaciones).save(argThat(e -> e.getFuentes().isEmpty()
                && ExplicacionGenerador.SIN_FUENTES.equals(e.getResumen()) && e.getGeneradaEn() != null));
        verifyNoInteractions(groq);
    }
}
