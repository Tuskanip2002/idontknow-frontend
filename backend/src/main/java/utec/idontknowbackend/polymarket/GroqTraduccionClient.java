package utec.idontknowbackend.polymarket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import utec.idontknowbackend.exceptions.ExternalServiceException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GroqTraduccionClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public GroqTraduccionClient(@Value("${groq.api.key}") String apiKey,
                                @Value("${groq.api.url:https://api.groq.com/openai/v1}") String baseUrl,
                                @Value("${groq.model:openai/gpt-oss-20b}") String modelo) {
        this.apiKey = apiKey;
        this.modelo = modelo;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String traducir(String preguntaOriginal, BigDecimal probabilidadActual) {
        String prompt = """
                Convierte esta pregunta de mercado de predicción en un titular de noticia corto,
                en español neutro, tipo diario. Máximo 20 palabras, sin comillas, sin explicar el porcentaje
                (el porcentaje se muestra aparte). Pregunta original: "%s"
                """.formatted(preguntaOriginal);

        ChatRequest request = new ChatRequest(modelo, List.of(new Message("user", prompt)), 0.3);

        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices().isEmpty()) {
                return preguntaOriginal; // fallback: mejor mostrar el original que fallar la portada
            }
            return response.choices().get(0).message().content().trim();
        } catch (Exception ex) {
            // no lanzamos ExternalServiceException aquí a propósito: si Groq falla,
            // preferimos que la portada se arme igual con el texto en inglés como fallback
            return preguntaOriginal;
        }
    }

    private record Message(String role, String content) {}
    private record ChatRequest(String model, List<Message> messages, double temperature) {}
    private record Choice(Message message) {}
    private record ChatResponse(List<Choice> choices) {}
}
