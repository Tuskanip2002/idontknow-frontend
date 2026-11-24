package utec.idontknowbackend.explicacion.infrastructure;

import org.junit.jupiter.api.Test;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolymarketComentariosClientTest {

    private static final String COMENTARIOS = """
            [
              {"id":"1","body":"lol","reactionCount":50,"createdAt":"2026-11-23T10:00:00Z"},
              {"id":"2","body":"New polling data shows a clear shift toward the challenger this week.","reactionCount":12,"createdAt":"2026-11-23T11:00:00Z"},
              {"id":"3","body":"The debate performance was weaker than expected, odds should move.","reactionCount":30,"createdAt":"2026-11-22T09:30:00.123Z"}
            ]
            """;

    @Test
    void ordenaPorReaccionesYDescartaComentariosCortos() {
        List<FuenteExplicacion> fuentes = PolymarketComentariosClient.leerComentarios(COMENTARIOS, 5);

        assertEquals(2, fuentes.size());
        assertTrue(fuentes.get(0).getTitulo().startsWith("The debate performance"));
        assertEquals(TipoFuente.COMENTARIO, fuentes.get(0).getTipo());
        assertNull(fuentes.get(0).getUrl());
        assertEquals(22, fuentes.get(0).getFecha().getDayOfMonth());
    }

    @Test
    void mercadosDemoNoConsultanPolymarket() {
        var client = new PolymarketComentariosClient("http://127.0.0.1:1");
        assertTrue(client.obtenerComentarios("demo-3", 3).isEmpty());
        assertTrue(client.obtenerComentarios(null, 3).isEmpty());
    }

    @Test
    void respuestaInvalidaCuentaComoSinComentarios() {
        assertTrue(PolymarketComentariosClient.leerComentarios("<html>error</html>", 3).isEmpty());
    }
}
