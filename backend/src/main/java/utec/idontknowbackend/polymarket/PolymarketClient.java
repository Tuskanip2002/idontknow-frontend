package utec.idontknowbackend.polymarket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import utec.idontknowbackend.exceptions.ExternalServiceException;

import java.util.List;

@Component
public class PolymarketClient {

    private final RestClient restClient;

    public PolymarketClient(@Value("${polymarket.api.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<PolymarketMarketDTO> obtenerMercadosActivos() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/markets")
                            .queryParam("active", true)
                            .queryParam("closed", false)
                            .queryParam("limit", 100)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PolymarketMarketDTO>>() {});
        } catch (Exception ex) {
            throw new ExternalServiceException("Error consultando Polymarket: " + ex.getMessage());
        }
    }
}
