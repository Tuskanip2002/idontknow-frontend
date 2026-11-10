package utec.idontknowbackend.polymarket;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GroqTraduccionClientTest {
    @Test
    void leeLaRespuestaCompletaDelProveedor() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = """
                    {"id":"test","object":"chat.completion","model":"test",
                     "choices":[{"index":0,"message":{"role":"assistant","content":"El mercado prevé lluvias","refusal":null},"finish_reason":"stop"}],
                     "usage":{"total_tokens":40},"x_groq":{"id":"request-test"}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) { output.write(response); }
        });
        server.start();
        try {
            var client = new GroqTraduccionClient("test", "http://127.0.0.1:" + server.getAddress().getPort(), "test");
            assertEquals("El mercado prevé lluvias", client.traducir("Will it rain?", BigDecimal.ONE));
        } finally { server.stop(0); }
    }
}
