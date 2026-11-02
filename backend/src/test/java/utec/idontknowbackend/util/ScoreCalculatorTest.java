package utec.idontknowbackend.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    @Test
    void calcular_sinBonus_devuelveSoloElCambio() {
        double puntaje = ScoreCalculator.calcular(new BigDecimal("0.10"), false, false);
        assertThat(puntaje).isEqualTo(10.0);
    }

    @Test
    void calcular_conCruceDeUmbral_sumaBonus() {
        double puntaje = ScoreCalculator.calcular(new BigDecimal("0.10"), true, false);
        assertThat(puntaje).isEqualTo(25.0); // 10 + 15
    }

    @Test
    void calcular_conAmbosBonus_sumaLosDos() {
        double puntaje = ScoreCalculator.calcular(new BigDecimal("0.05"), true, true);
        assertThat(puntaje).isEqualTo(30.0); // 5 + 15 + 10
    }

    @Test
    void calcular_conCambioNegativo_usaValorAbsoluto() {
        double puntaje = ScoreCalculator.calcular(new BigDecimal("-0.20"), false, false);
        assertThat(puntaje).isEqualTo(20.0);
    }
}
