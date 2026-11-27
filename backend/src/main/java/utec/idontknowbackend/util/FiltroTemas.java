package utec.idontknowbackend.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Detecta mercados de chismes o de contenido sexual para que no lleguen a la portada.
 * Revisa la pregunta original en inglés de Polymarket, con palabras completas
 * (por ejemplo, "sex" no coincide con "Sussex").
 */
public final class FiltroTemas {

    private static final List<String> PALABRAS = List.of(
            "affair", "affairs", "romance", "romantic", "girlfriend", "boyfriend",
            "dating", "divorce", "cheating", "cheated", "mistress", "hookup",
            "pregnant", "pregnancy", "onlyfans", "nude", "nudes", "naked", "porn",
            "sex", "sexual", "kiss", "come forward", "comes forward", "accuser", "accusers");

    private static final Pattern PATRON = Pattern.compile(
            "\\b(" + String.join("|", PALABRAS.stream().map(Pattern::quote).toList()) + ")\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private FiltroTemas() {}

    public static boolean esSensacionalista(String pregunta) {
        return pregunta != null && PATRON.matcher(pregunta).find();
    }
}
