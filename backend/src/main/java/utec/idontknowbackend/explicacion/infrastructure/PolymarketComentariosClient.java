package utec.idontknowbackend.explicacion.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.Texto;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lee los comentarios públicos de un mercado desde el endpoint /comments de la API Gamma. */
@Component
@Slf4j
public class PolymarketComentariosClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int LARGO_MINIMO = 25; // descarta comentarios tipo "lol" o "yes!!"

    private final RestClient restClient;

    public PolymarketComentariosClient(@Value("${polymarket.api.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Los comentarios con más reacciones. Lista vacía si el mercado no tiene o la API falla. */
    public List<FuenteExplicacion> obtenerComentarios(String polymarketId, int maximo) {
        // los mercados del perfil demo ("demo-3") no existen en Polymarket
        if (polymarketId == null || !polymarketId.matches("\\d+")) {
            return List.of();
        }
        List<FuenteExplicacion> comentarios = consultar("market", polymarketId, maximo);
        if (!comentarios.isEmpty()) {
            return comentarios;
        }
        // en Polymarket la conversación suele colgar del evento que agrupa al mercado
        String eventoId = buscarEvento(polymarketId);
        return eventoId == null ? List.of() : consultar("Event", eventoId, maximo);
    }

    private List<FuenteExplicacion> consultar(String tipoEntidad, String id, int maximo) {
        try {
            String body = restClient.get()
                    .uri(builder -> builder.path("/comments")
                            .queryParam("parent_entity_type", tipoEntidad)
                            .queryParam("parent_entity_id", id)
                            .queryParam("limit", 40)
                            .queryParam("order", "createdAt")
                            .queryParam("ascending", false)
                            .build())
                    .retrieve()
                    .body(String.class);
            return leerComentarios(body, maximo);
        } catch (Exception ex) {
            log.warn("No se pudieron leer comentarios de Polymarket ({} {}): {}", tipoEntidad, id, ex.getMessage());
            return List.of();
        }
    }

    private String buscarEvento(String marketId) {
        try {
            String body = restClient.get().uri("/markets/{id}", marketId).retrieve().body(String.class);
            JsonNode eventos = MAPPER.readTree(body).path("events");
            if (eventos.isArray() && !eventos.isEmpty()) {
                String id = eventos.get(0).path("id").asText("");
                return id.isBlank() ? null : id;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    static List<FuenteExplicacion> leerComentarios(String body, int maximo) {
        if (body == null || !body.trim().startsWith("[")) {
            return List.of();
        }
        try {
            List<JsonNode> nodos = new ArrayList<>();
            MAPPER.readTree(body).forEach(nodos::add);
            return nodos.stream()
                    .filter(n -> n.path("body").asText("").trim().length() >= LARGO_MINIMO)
                    .sorted(Comparator.comparingInt((JsonNode n) -> n.path("reactionCount").asInt(0)).reversed())
                    .limit(maximo)
                    .map(n -> FuenteExplicacion.builder()
                            .tipo(TipoFuente.COMENTARIO)
                            .titulo(Texto.recortar(n.path("body").asText(""), 300))
                            .medio("Comentario en Polymarket")
                            .fecha(leerFecha(n.path("createdAt").asText(null)))
                            .build())
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static LocalDateTime leerFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(valor).toLocalDateTime();
        } catch (Exception ex) {
            return null;
        }
    }
}
