package utec.idontknowbackend.explicacion.infrastructure;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GdeltClientTest {

    @Test
    void un429PausaLasConsultasPosteriores() throws Exception {
        AtomicInteger consultas = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/doc", exchange -> {
            consultas.incrementAndGet();
            exchange.getResponseHeaders().add("Retry-After", "120");
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();
        try {
            var client = new GdeltClient("http://127.0.0.1:" + server.getAddress().getPort() + "/doc");
            assertTrue(client.buscarNoticias("Netanyahu", 4).isEmpty());
            assertTrue(client.buscarNoticias("NVIDIA", 4).isEmpty());
            assertEquals(1, consultas.get(), "No debe insistir durante el límite del proveedor");
        } finally {
            server.stop(0);
        }
    }

    private static final String RESPUESTA = """
            {"articles":[
              {"url":"https://news.example.com/a","title":"Senate vote delayed","seendate":"20261123T143000Z","domain":"news.example.com"},
              {"url":"https://other.example.com/b","title":"Senate vote delayed","seendate":"20261123T150000Z","domain":"other.example.com"},
              {"url":"https://third.example.com/c","title":"Polls tighten ahead of runoff","seendate":"20261122T090000Z","domain":"third.example.com"}
            ]}
            """;

    @Test
    void leeArticulosYDescartaTitulosRepetidos() throws Exception {
        AtomicReference<String> consultaRecibida = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v2/doc/doc", exchange -> {
            consultaRecibida.set(exchange.getRequestURI().getRawQuery());
            byte[] body = RESPUESTA.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            var client = new GdeltClient("http://127.0.0.1:" + server.getAddress().getPort() + "/api/v2/doc/doc");
            List<FuenteExplicacion> fuentes = client.buscarNoticias("Senate runoff", 4);

            assertEquals(2, fuentes.size());
            assertEquals(TipoFuente.NOTICIA, fuentes.get(0).getTipo());
            assertEquals("news.example.com", fuentes.get(0).getMedio());
            assertEquals(2026, fuentes.get(0).getFecha().getYear());
            assertTrue(consultaRecibida.get().contains("query=Senate%20runoff"));
            assertTrue(consultaRecibida.get().contains("mode=ArtList"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void textoPlanoCuentaComoSinResultados() {
        assertTrue(GdeltClient.leerArticulos("Your search contained a phrase that was too short.", 4).isEmpty());
        assertTrue(GdeltClient.leerArticulos("{}", 4).isEmpty());
    }

    @Test
    void consultaVaciaNoLlamaAGdelt() {
        var client = new GdeltClient("http://127.0.0.1:1/no-existe");
        assertTrue(client.buscarNoticias("", 4).isEmpty());
    }
}
