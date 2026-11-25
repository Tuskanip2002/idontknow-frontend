package utec.idontknowbackend.explicacion.infrastructure;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class NoticiasRssClientTest {
    private static final Instant AHORA = Instant.parse("2026-11-25T04:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    private static String item(String titulo, String enlace, String fecha, String medio) {
        return "<item><title>" + titulo + "</title><link>" + enlace + "</link><pubDate>" + fecha
                + "</pubDate><source>" + medio + "</source></item>";
    }

    private static String rss(String items) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rss version=\"2.0\"><channel>" + items + "</channel></rss>";
    }

    @Test
    void conservaMedioEnlaceYFechaDeduplicaYOrdenaPorRecencia() {
        String xml = rss(item("Noticia anterior - Diario A", "https://news.example/a", "Mon, 23 Nov 2026 10:00:00 GMT", "Diario A")
                + item("Noticia reciente &amp; contexto - Diario B", "https://news.example/b", "Wed, 25 Nov 2026 03:00:00 GMT", "Diario B")
                + item("Noticia anterior - Diario C", "https://news.example/c", "Tue, 24 Nov 2026 10:00:00 GMT", "Diario C"));
        var fuentes = NoticiasRssClient.leerArticulos(xml, 4, AHORA);
        assertEquals(2, fuentes.size());
        assertEquals("Noticia reciente & contexto", fuentes.get(0).getTitulo());
        assertEquals("Diario B", fuentes.get(0).getMedio());
        assertEquals("https://news.example/b", fuentes.get(0).getUrl());
        assertEquals(TipoFuente.NOTICIA, fuentes.get(0).getTipo());
        assertEquals(LocalDateTime.of(2026, 11, 24, 22, 0), fuentes.get(0).getFecha());
        assertEquals(1, NoticiasRssClient.leerArticulos(xml, 1, AHORA).size());
    }

    @Test
    void descartaArticulosViejosFuturosSinFechaSinMedioOConEnlaceInseguro() {
        String xml = rss(item("Viejo", "https://news.example/a", "Sun, 01 Nov 2026 10:00:00 GMT", "Diario")
                + item("Futuro", "https://news.example/b", "Thu, 26 Nov 2026 10:00:00 GMT", "Diario")
                + item("Sin fecha", "https://news.example/c", "", "Diario")
                + item("Sin medio", "https://news.example/d", "Tue, 24 Nov 2026 10:00:00 GMT", "")
                + item("Enlace inseguro", "javascript:alert(1)", "Tue, 24 Nov 2026 10:00:00 GMT", "Diario"));
        assertTrue(NoticiasRssClient.leerArticulos(xml, 4, AHORA).isEmpty());
    }

    @Test
    void noDescargaEntidadesExternasDelXml() throws Exception {
        AtomicInteger peticiones = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/external", exchange -> {
            peticiones.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            String xml = "<!DOCTYPE rss [<!ENTITY externa SYSTEM \"http://127.0.0.1:"
                    + server.getAddress().getPort() + "/external\">]>"
                    + "<rss><channel><item><title>&externa;</title></item></channel></rss>";
            assertTrue(NoticiasRssClient.leerArticulos(xml, 4, AHORA).isEmpty());
            assertEquals(0, peticiones.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void consultaRssConParametrosCodificadosYVentanaDeTresDias() throws Exception {
        AtomicReference<String> query = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/rss/search", exchange -> {
            query.set(exchange.getRequestURI().getRawQuery());
            byte[] body = rss(item("Noticia", "https://news.example/a", "Tue, 24 Nov 2026 10:00:00 GMT", "Diario"))
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            var client = new NoticiasRssClient("http://127.0.0.1:" + server.getAddress().getPort() + "/rss/search", true, RELOJ);
            assertEquals(1, client.buscarNoticias("NVIDIA largest company", 4).size());
            assertTrue(URLDecoder.decode(query.get(), StandardCharsets.UTF_8).contains("q=NVIDIA largest company when:3d"));
            assertTrue(query.get().contains("ceid=US"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void deshabilitadoOConsultaVaciaNoHacePeticiones() throws Exception {
        AtomicInteger peticiones = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            peticiones.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort();
            assertTrue(new NoticiasRssClient(url, false, RELOJ).buscarNoticias("Netanyahu", 4).isEmpty());
            var client = new NoticiasRssClient(url, true, RELOJ);
            assertTrue(client.buscarNoticias("", 4).isEmpty());
            assertTrue(client.buscarNoticias("Netanyahu", 0).isEmpty());
            assertEquals(0, peticiones.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void unaCaidaDelServicioNoInterrumpeLaGeneracion() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();
        try {
            var client = new NoticiasRssClient("http://127.0.0.1:" + server.getAddress().getPort(), true, RELOJ);
            assertTrue(client.buscarNoticias("Netanyahu", 4).isEmpty());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rechazaXmlInvalidoYRespuestasDemasiadoGrandes() {
        assertTrue(NoticiasRssClient.leerArticulos("<rss>", 4, AHORA).isEmpty());
        assertTrue(NoticiasRssClient.leerArticulos("x".repeat(1_000_001), 4, AHORA).isEmpty());
    }
}
