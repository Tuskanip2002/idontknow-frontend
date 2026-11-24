package utec.idontknowbackend.polymarket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GroqTraduccionClient {

    /** Respuesta que pedimos al modelo cuando las fuentes no explican el movimiento. */
    static final String SIN_EVIDENCIA = "SIN_EVIDENCIA";

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

        String respuesta = completar(prompt, 0.3);
        // si Groq falla, preferimos que la portada se arme igual con el texto en inglés como fallback
        return respuesta == null ? preguntaOriginal : respuesta;
    }

    /**
     * Resume los posibles factores detrás de un movimiento usando SOLO las fuentes recibidas.
     * Devuelve null si no hay fuentes, si el modelo dice que no hay evidencia o si Groq falla:
     * en esos casos quien llama muestra un mensaje honesto en vez de inventar una causa.
     *
     * @param probabilidadPct probabilidad actual en porcentaje (ej. 62.0)
     * @param cambioPp        cambio desde ayer en puntos porcentuales (ej. 9.5 o -4.0)
     * @param fuentes         textos de las fuentes; se numeran en el orden recibido
     */
    public String resumirMovimiento(String pregunta, BigDecimal probabilidadPct, BigDecimal cambioPp,
                                    List<String> fuentes) {
        if (fuentes == null || fuentes.isEmpty()) {
            return null;
        }
        StringBuilder lista = new StringBuilder();
        for (int i = 0; i < fuentes.size(); i++) {
            lista.append('[').append(i + 1).append("] ").append(fuentes.get(i)).append('\n');
        }

        String prompt = """
                Eres editor de un diario en español neutro que explica mercados de predicción.
                Pregunta del mercado (en inglés): "%s"
                Probabilidad actual: %s %%. Cambio desde ayer: %s puntos porcentuales.

                Fuentes recientes, numeradas:
                %s
                Escribe en 2 o 3 oraciones (máximo 80 palabras) los posibles factores que podrían explicar
                este movimiento. Usa SOLO información de las fuentes y cita cada afirmación con su número
                entre corchetes, por ejemplo [2]. No inventes datos, no des consejos de apuesta y no presentes
                una causa como segura: usa expresiones como "podría", "coincide con" o "según".
                Si las fuentes no tienen relación con la pregunta, responde exactamente %s.
                """.formatted(pregunta, probabilidadPct, cambioPp, lista, SIN_EVIDENCIA);

        String respuesta = completar(prompt, 0.2);
        if (respuesta == null || respuesta.contains(SIN_EVIDENCIA)) {
            return null;
        }
        return respuesta;
    }

    /** Llama a Groq y devuelve el texto de la respuesta, o null si falla o viene vacía. */
    private String completar(String prompt, double temperatura) {
        ChatRequest request = new ChatRequest(modelo, List.of(new Message("user", prompt)), temperatura);
        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return null;
            }
            String contenido = response.choices().get(0).message().content();
            return contenido == null || contenido.isBlank() ? null : contenido.trim();
        } catch (Exception ex) {
            // no lanzamos ExternalServiceException a propósito: cada uso decide su propio fallback
            return null;
        }
    }

    private record Message(String role, String content) {}
    private record ChatRequest(String model, List<Message> messages, double temperature) {}
    private record Choice(Message message) {}
    private record ChatResponse(List<Choice> choices) {}
}
