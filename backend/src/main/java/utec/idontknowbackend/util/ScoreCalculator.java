package utec.idontknowbackend.util;

import java.math.BigDecimal;

public class ScoreCalculator {

    private static final double BONUS_CRUCE_UMBRAL = 15.0;
    private static final double BONUS_RESOLUCION_CERCANA = 10.0;

    private ScoreCalculator() {}

    public static double calcular(BigDecimal cambioDesdeAyer, boolean cruzoUmbral, boolean seResuelvePronto) {
        double puntaje = cambioDesdeAyer.abs().doubleValue() * 100; // puntos porcentuales de cambio
        if (cruzoUmbral) puntaje += BONUS_CRUCE_UMBRAL;
        if (seResuelvePronto) puntaje += BONUS_RESOLUCION_CERCANA;
        return puntaje;
    }
}
