package utec.idontknowbackend.explicacion.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PalabrasClaveTest {

    @Test
    void priorizaNombresPropiosYDescartaPalabrasVacias() {
        assertEquals("Trump presidential election",
                PalabrasClave.consulta("Will Trump win the 2028 presidential election?"));
    }

    @Test
    void quitaPosesivosYMeses() {
        assertEquals("Fed interest rates",
                PalabrasClave.consulta("Will the Fed's interest rates fall in December?"));
    }

    @Test
    void devuelveVacioSiNoHayPregunta() {
        assertEquals("", PalabrasClave.consulta(null));
        assertEquals("", PalabrasClave.consulta("   "));
    }
}
