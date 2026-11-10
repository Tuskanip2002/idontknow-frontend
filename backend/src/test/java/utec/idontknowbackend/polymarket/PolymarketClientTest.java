package utec.idontknowbackend.polymarket;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;

class PolymarketClientTest {
    @Test
    void consultaLosCuatroTemasYConservaSuCategoria() throws Exception {
        Set<String> tags = ConcurrentHashMap.newKeySet();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String body;
            if (path.startsWith("/tags/slug/")) {
                String slug = path.substring("/tags/slug/".length());
                tags.add(slug);
                body = "{\"id\":\"" + slug + "\"}";
            } else {
                String query = exchange.getRequestURI().getQuery();
                String id = java.util.Arrays.stream(query.split("&"))
                        .filter(value -> value.startsWith("tag_id="))
                        .findFirst().orElseThrow().substring(7);
                body = "[{\"id\":\"" + id + "\",\"question\":\"Example?\"}]";
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        try {
            var markets = new PolymarketClient("http://127.0.0.1:" + server.getAddress().getPort())
                    .obtenerMercadosActivos();
            assertEquals(Set.of("politics", "economy", "sports", "tech"), tags);
            assertEquals(Set.of("POLITICA", "ECONOMIA", "DEPORTES", "TECNOLOGIA"),
                    markets.stream().map(PolymarketMarketDTO::getCategoriaFuente).collect(java.util.stream.Collectors.toSet()));
        } finally { server.stop(0); }
    }
}
