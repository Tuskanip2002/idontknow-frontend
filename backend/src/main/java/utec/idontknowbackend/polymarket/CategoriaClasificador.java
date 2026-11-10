package utec.idontknowbackend.polymarket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.categoria.model.Categoria;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CategoriaClasificador {

    // las preguntas de Polymarket vienen en inglés, por eso las palabras clave están en inglés
    private static final Map<String, Pattern> PALABRAS_CLAVE = Map.of(
            "POLITICA", Pattern.compile("\\b(election|elected|president|presidential|senate|congress|governor|minister|parliament|vote|party|trump|biden|putin|zelensky|nominee|nomination|xi jinping|impeach\\w*|war|ceasefire|nato)\\b"),
            "ECONOMIA", Pattern.compile("\\b(fed|interest rates?|inflation|recession|gdp|oil|stocks?|s&p|nasdaq|tariffs?|dollar|bitcoin|btc|ethereum|eth|crypto|price|economy|unemployment|market cap)\\b"),
            "DEPORTES", Pattern.compile("\\b(nba|nfl|mlb|nhl|fifa|world cup|champions league|premier league|la liga|super bowl|olympics?|f1|formula 1|ufc|tennis|wimbledon|copa|championship|finals?|match)\\b"),
            "TECNOLOGIA", Pattern.compile("\\b(ai|openai|chatgpt|gpt-?\\d*|apple|google|tesla|spacex|nvidia|microsoft|meta|iphone|starship|launch|tiktok|anthropic)\\b")
    );

    private final CategoriaRepository categoriaRepository;

    public Set<Categoria> clasificar(String pregunta, String categoriaFuente) {
        Set<Categoria> resultado = clasificar(pregunta);
        if (categoriaFuente != null) {
            categoriaRepository.findByNombre(categoriaFuente).ifPresent(resultado::add);
        }
        return resultado;
    }

    public Set<Categoria> clasificar(String pregunta) {
        Set<Categoria> resultado = new HashSet<>();
        if (pregunta == null) return resultado;

        String texto = pregunta.toLowerCase(Locale.ROOT);
        PALABRAS_CLAVE.forEach((nombre, patron) -> {
            if (patron.matcher(texto).find()) {
                categoriaRepository.findByNombre(nombre).ifPresent(resultado::add);
            }
        });
        return resultado;
    }
}

