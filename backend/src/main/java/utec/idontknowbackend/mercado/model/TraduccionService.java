package utec.idontknowbackend.mercado.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.exceptions.ExternalServiceException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.polymarket.GroqTraduccionClient;

@Service
@RequiredArgsConstructor
public class TraduccionService {
    private final MercadoRepository mercados;
    private final GroqTraduccionClient groq;

    public record Traduccion(String original, String espanol) {}

    @Transactional
    public Traduccion traducir(Long id) {
        Mercado mercado = mercados.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado no encontrado"));
        if (mercado.getPreguntaEspanol() == null) {
            String texto = groq.traducir(mercado.getPreguntaOriginal(), mercado.getProbabilidadActual());
            if (texto == null || texto.isBlank() || texto.equals(mercado.getPreguntaOriginal())) {
                throw new ExternalServiceException("La traducción no está disponible. Inténtalo más tarde.");
            }
            mercado.setPreguntaEspanol(texto);
            mercados.save(mercado);
        }
        return new Traduccion(mercado.getPreguntaOriginal(), mercado.getPreguntaEspanol());
    }
}
