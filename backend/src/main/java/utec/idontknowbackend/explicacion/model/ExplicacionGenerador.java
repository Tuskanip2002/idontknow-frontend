package utec.idontknowbackend.explicacion.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.explicacion.infrastructure.ExplicacionRepository;
import utec.idontknowbackend.explicacion.infrastructure.GdeltClient;
import utec.idontknowbackend.explicacion.infrastructure.PolymarketComentariosClient;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.polymarket.GroqTraduccionClient;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Arma la explicación de cada titular: busca noticias (GDELT) y comentarios (Polymarket),
 * y le pide a Groq un resumen que solo use esas fuentes. Se ejecuta al generar la portada
 * de las 6:00, así la web y la app solo leen de la base de datos y la demo no depende
 * de servicios externos en vivo.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExplicacionGenerador {

    private static final int MAX_NOTICIAS = 4;
    private static final int MAX_COMENTARIOS = 3;
    private static final long PAUSA_ENTRE_TITULARES_MS = 5_500; // GDELT pide ~1 consulta cada 5 s
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    static final String SIN_FUENTES =
            "No hay fuentes disponibles para explicar este movimiento en este momento. "
                    + "No podemos atribuirle una causa con la información obtenida.";
    static final String SIN_CAUSA_CLARA =
            "Las fuentes recientes no explican con claridad este movimiento. "
                    + "Puede deberse a información que todavía no aparece en las noticias.";

    private final TitularRepository titularRepository;
    private final EdicionRepository edicionRepository;
    private final ExplicacionRepository explicacionRepository;
    private final GdeltClient gdeltClient;
    private final PolymarketComentariosClient comentariosClient;
    private final GroqTraduccionClient groqClient;

    @Async
    public void generarEnSegundoPlano(Long edicionId, boolean soloFaltantes) {
        generarParaEdicion(edicionId, soloFaltantes);
    }

    /** Al arrancar el servidor completa las explicaciones que falten en la portada de hoy (por ejemplo, tras un despliegue). */
    @Async
    public void generarHoySiFalta() {
        edicionRepository.findByFecha(LocalDate.now(LIMA))
                .ifPresent(edicion -> generarParaEdicion(edicion.getId(), true));
    }

    public int generarParaEdicion(Long edicionId, boolean soloFaltantes) {
        int generadas = 0;
        for (Titular titular : titularRepository.findConMercadoByEdicionId(edicionId)) {
            if (soloFaltantes && explicacionRepository.existsByTitularId(titular.getId())) {
                continue;
            }
            if (generadas > 0) {
                pausar();
            }
            try {
                generar(titular);
                generadas++;
            } catch (Exception ex) {
                // un titular que falla no debe impedir que se expliquen los demás
                log.warn("No se pudo generar la explicación del titular {}: {}", titular.getId(), ex.getMessage());
            }
        }
        log.info("Explicaciones generadas para la edición {}: {}", edicionId, generadas);
        return generadas;
    }

    public void generar(Titular titular) {
        Mercado mercado = titular.getMercado();
        Explicacion explicacion = explicacionRepository.findByTitularId(titular.getId())
                .orElseGet(() -> Explicacion.builder().titular(titular).build());

        List<FuenteExplicacion> fuentes = new ArrayList<>();
        fuentes.addAll(gdeltClient.buscarNoticias(PalabrasClave.consulta(mercado.getPreguntaOriginal()), MAX_NOTICIAS));
        fuentes.addAll(comentariosClient.obtenerComentarios(mercado.getPolymarketId(), MAX_COMENTARIOS));

        if (fuentes.isEmpty() && !explicacion.getFuentes().isEmpty()) {
            log.warn("Sin fuentes nuevas para el titular {}; se conservan las {} fuentes previas y su fecha de generación",
                    titular.getId(), explicacion.getFuentes().size());
            return;
        }
        // Un proveedor puede fallar mientras el otro sí devuelve fuentes.
        for (TipoFuente tipo : TipoFuente.values()) {
            if (fuentes.stream().noneMatch(f -> f.getTipo() == tipo)) {
                List<FuenteExplicacion> anteriores = explicacion.getFuentes().stream()
                        .filter(f -> f.getTipo() == tipo).toList();
                if (!anteriores.isEmpty()) {
                    fuentes.addAll(anteriores);
                    log.warn("Se conservan {} fuentes previas de tipo {} para el titular {}",
                            anteriores.size(), tipo, titular.getId());
                }
            }
        }

        String resumen = fuentes.isEmpty() ? null : groqClient.resumirMovimiento(
                mercado.getPreguntaOriginal(),
                enPorcentaje(titular.getProbabilidadHoy()),
                enPorcentaje(titular.getCambioDesdeAyer()),
                fuentes.stream().map(ExplicacionGenerador::describir).toList());

        explicacion.setResumen(resumen != null ? resumen : (fuentes.isEmpty() ? SIN_FUENTES : SIN_CAUSA_CLARA));
        explicacion.getFuentes().clear();
        explicacion.getFuentes().addAll(fuentes);
        explicacion.setGeneradaEn(LocalDateTime.now(LIMA));
        explicacionRepository.save(explicacion);
        log.info("Explicación del titular {} guardada con {} fuentes ({} noticias, {} comentarios)",
                titular.getId(), fuentes.size(),
                fuentes.stream().filter(f -> f.getTipo() == TipoFuente.NOTICIA).count(),
                fuentes.stream().filter(f -> f.getTipo() == TipoFuente.COMENTARIO).count());
    }

    static String describir(FuenteExplicacion fuente) {
        return switch (fuente.getTipo()) {
            case NOTICIA -> "Noticia" + (fuente.getMedio() != null ? " (" + fuente.getMedio() + ")" : "")
                    + ": " + fuente.getTitulo();
            case COMENTARIO -> "Comentario de un participante en Polymarket: " + fuente.getTitulo();
        };
    }

    private static BigDecimal enPorcentaje(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
    }

    private static void pausar() {
        try {
            Thread.sleep(PAUSA_ENTRE_TITULARES_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
