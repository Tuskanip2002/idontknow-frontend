package utec.idontknowbackend.explicacion.model;

public final class Texto {

    private Texto() {
    }

    /** Recorta un texto para que entre en su columna, agregando "…" si se cortó. */
    public static String recortar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().replaceAll("\\s+", " ");
        if (limpio.length() <= maximo) {
            return limpio;
        }
        return limpio.substring(0, maximo - 1).trim() + "…";
    }
}
