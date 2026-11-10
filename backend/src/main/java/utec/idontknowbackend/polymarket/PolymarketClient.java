package utec.idontknowbackend.polymarket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import utec.idontknowbackend.exceptions.ExternalServiceException;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PolymarketClient {

    private final RestClient restClient;

    public PolymarketClient(@Value("${polymarket.api.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<PolymarketMarketDTO> obtenerMercadosActivos() {
        Map<String, PolymarketMarketDTO> mercados = new LinkedHashMap<>();
        for (String slug : List.of("politics", "economy", "sports", "tech")) {
            Tag tag = restClient.get().uri("/tags/slug/" + slug).retrieve().body(Tag.class);
            if (tag == null || tag.id() == null) {
                throw new ExternalServiceException("Polymarket no devolvió la categoría " + slug);
            }
            for (PolymarketMarketDTO mercado : obtenerPorCategoria(tag.id())) {
                mercado.setCategoriaFuente(switch (slug) {
                    case "politics" -> "POLITICA";
                    case "economy" -> "ECONOMIA";
                    case "sports" -> "DEPORTES";
                    default -> "TECNOLOGIA";
                });
                mercados.putIfAbsent(mercado.getId(), mercado);
            }
        }
        return List.copyOf(mercados.values());
    }

    private record Tag(String id) {}

    private List<PolymarketMarketDTO> obtenerPorCategoria(String tagId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/markets")
                            .queryParam("active", true)
                            .queryParam("closed", false)
                            .queryParam("tag_id", tagId)
                            .queryParam("order", "volumeNum")
                            .queryParam("ascending", false)
                            .queryParam("limit", 50)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PolymarketMarketDTO>>() {});
        } catch (Exception ex) {
            throw new ExternalServiceException("Error consultando Polymarket: " + ex.getMessage());
        }
    }
}
