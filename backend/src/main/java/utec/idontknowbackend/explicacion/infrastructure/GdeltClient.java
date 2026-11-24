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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Busca noticias recientes en la API DOC 2.0 de GDELT (gratuita, sin API key).
 * GDELT traduce al inglés prensa de 65 idiomas, así que buscamos con las palabras
 * clave en inglés de la pregunta de Polymarket.
 */
@Component
@Slf4j
public class GdeltClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FECHA_GDELT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final RestClient restClient;

    public GdeltClient(@Value("${gdelt.api.url:https://api.gdeltproject.org/api/v2/doc/doc}") String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    /** Noticias de los últimos 3 días. Devuelve una lista vacía si GDELT falla o no encuentra nada. */
    public List<FuenteExplicacion> buscarNoticias(String consulta, int maximo) {
        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(builder -> builder
                            .queryParam("query", "{consulta}")
                            .queryParam("mode", "ArtList")
                            .queryParam("format", "json")
                            .queryParam("sort", "DateDesc")
                            .queryParam("timespan", "3d")
                            .queryParam("maxrecords", maximo * 3)
                            .build(consulta))
                    .retrieve()
                    .body(String.class);
            return leerArticulos(body, maximo);
        } catch (Exception ex) {
            log.warn("GDELT no respondió para '{}': {}", consulta, ex.getMessage());
            return List.of();
        }
    }

    /** GDELT a veces responde texto plano (por ejemplo, si la búsqueda es muy corta): eso cuenta como "sin resultados". */
    static List<FuenteExplicacion> leerArticulos(String body, int maximo) {
        if (body == null || !body.trim().startsWith("{")) {
            return List.of();
        }
        try {
            List<FuenteExplicacion> fuentes = new ArrayList<>();
            Set<String> titulosVistos = new HashSet<>();
            for (JsonNode articulo : MAPPER.readTree(body).path("articles")) {
                String titulo = articulo.path("title").asText("").trim();
                String url = articulo.path("url").asText("").trim();
                // el mismo cable de agencia suele repetirse en varios medios
                if (titulo.isEmpty() || url.isEmpty() || !titulosVistos.add(titulo.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                fuentes.add(FuenteExplicacion.builder()
                        .tipo(TipoFuente.NOTICIA)
                        .titulo(Texto.recortar(titulo, 300))
                        .url(Texto.recortar(url, 1000))
                        .medio(Texto.recortar(articulo.path("domain").asText(null), 200))
                        .fecha(leerFecha(articulo.path("seendate").asText(null)))
                        .build());
                if (fuentes.size() >= maximo) {
                    break;
                }
            }
            return fuentes;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static LocalDateTime leerFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor, FECHA_GDELT);
        } catch (Exception ex) {
            return null;
        }
    }
}
