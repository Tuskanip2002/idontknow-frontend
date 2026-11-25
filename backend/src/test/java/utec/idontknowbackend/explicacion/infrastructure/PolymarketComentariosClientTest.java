package utec.idontknowbackend.explicacion.infrastructure;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.util.List;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolymarketComentariosClientTest {

    @Test
    void resuelveElEventoDesdeElListadoYConsultaComentariosEvent() throws Exception {
        List<String> solicitudes = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String ruta = exchange.getRequestURI().toString();
            solicitudes.add(ruta);
            String body = ruta.equals("/markets?id=682705")
                    ? "[{\"id\":\"682705\",\"events\":[{\"id\":\"81557\"}]}]" : COMENTARIOS;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        try {
            var client = new PolymarketComentariosClient("http://127.0.0.1:" + server.getAddress().getPort());
            List<FuenteExplicacion> fuentes = client.obtenerComentarios("682705", 3);
            assertEquals(2, fuentes.size());
            assertEquals(2, solicitudes.size());
            assertEquals("/markets?id=682705", solicitudes.get(0));
            assertTrue(solicitudes.get(1).startsWith("/comments?"));
            assertTrue(solicitudes.get(1).contains("parent_entity_type=Event"));
            assertTrue(solicitudes.get(1).contains("parent_entity_id=81557"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void noUsaElEventoDeOtroMercadoSiLaApiIgnoraElFiltro() throws Exception {
        List<String> solicitudes = new ArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            solicitudes.add(exchange.getRequestURI().getPath());
            byte[] body = "[{\"id\":\"123\",\"events\":[{\"id\":\"456\"}]}]".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            var client = new PolymarketComentariosClient("http://127.0.0.1:" + server.getAddress().getPort());
            assertTrue(client.obtenerComentarios("682705", 3).isEmpty());
            assertEquals(List.of("/markets"), solicitudes);
        } finally {
            server.stop(0);
        }
    }

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
