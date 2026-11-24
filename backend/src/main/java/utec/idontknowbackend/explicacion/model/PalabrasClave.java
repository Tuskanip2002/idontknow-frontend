package utec.idontknowbackend.explicacion.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Convierte la pregunta de Polymarket (en inglés) en una búsqueda corta para GDELT.
 * Ej.: "Will Trump win the 2028 presidential election?" -> "Trump presidential election".
 * Prioriza los nombres propios (palabras con mayúscula) y descarta palabras vacías.
 */
public final class PalabrasClave {

    private static final int MAXIMO = 3;

    private static final Set<String> VACIAS = Set.of(
            "will", "the", "and", "for", "with", "this", "that", "which", "who", "what", "when", "how",
            "does", "did", "are", "was", "were", "been", "being", "have", "has", "had", "its", "his", "her",
            "their", "than", "more", "less", "any", "all", "from", "into", "over", "under", "above", "below",
            "between", "before", "after", "end", "out", "off", "not", "yes", "win", "wins", "reach", "hit",
            "much", "many", "least", "most", "next", "new", "per", "via", "get", "say", "says",
            "january", "february", "march", "april", "may", "june", "july", "august", "september",
            "october", "november", "december", "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep",
            "sept", "oct", "nov", "dec", "week", "month", "year", "today", "tomorrow"
    );

    private PalabrasClave() {
    }

    public static String consulta(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) {
            return "";
        }
        String limpia = pregunta
                .replaceAll("['’]s\\b", "")              // "Trump's" -> "Trump"
                .replaceAll("[^\\p{L}\\p{N} ]", " ");    // fuera signos, guiones y "?"

        Set<String> nombres = new LinkedHashSet<>();
        Set<String> otras = new LinkedHashSet<>();
        for (String palabra : limpia.trim().split("\\s+")) {
            String minuscula = palabra.toLowerCase(Locale.ROOT);
            if (palabra.length() < 3 || palabra.chars().allMatch(Character::isDigit) || VACIAS.contains(minuscula)) {
                continue;
            }
            if (Character.isUpperCase(palabra.charAt(0))) {
                nombres.add(palabra);
            } else {
                otras.add(palabra);
            }
        }

        List<String> elegidas = new ArrayList<>(nombres);
        elegidas.addAll(otras);
        return String.join(" ", elegidas.subList(0, Math.min(MAXIMO, elegidas.size())));
    }
}
