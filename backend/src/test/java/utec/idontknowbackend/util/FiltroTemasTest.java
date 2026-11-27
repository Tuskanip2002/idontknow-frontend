package utec.idontknowbackend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FiltroTemasTest {

    @Test
    void detectaChismesYContenidoSexual() {
        assertThat(FiltroTemas.esSensacionalista("Will Jensen Huang admit to another affair before December 31?")).isTrue();
        assertThat(FiltroTemas.esSensacionalista("Will a fourth woman come forward before November 30?")).isTrue();
        assertThat(FiltroTemas.esSensacionalista("Will Taylor Swift announce she is PREGNANT in 2026?")).isTrue();
    }

    @Test
    void dejaPasarTemasSerios() {
        assertThat(FiltroTemas.esSensacionalista("Will Benjamin Netanyahu be the next Prime Minister of Israel?")).isFalse();
        assertThat(FiltroTemas.esSensacionalista("Will AI solve the Riemann Hypothesis next?")).isFalse();
        assertThat(FiltroTemas.esSensacionalista("Strait of Hormuz traffic returns to normal by November 30?")).isFalse();
    }

    @Test
    void usaPalabrasCompletas() {
        assertThat(FiltroTemas.esSensacionalista("Will Sussex win the county championship?")).isFalse();
        assertThat(FiltroTemas.esSensacionalista("Who will win the Daytona 500?")).isFalse();
        assertThat(FiltroTemas.esSensacionalista(null)).isFalse();
    }
}
