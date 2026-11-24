package utec.idontknowbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.explicacion.infrastructure.ExplicacionRepository;
import utec.idontknowbackend.explicacion.model.Explicacion;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.TipoFuente;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfil demo: explicaciones de ejemplo, claramente marcadas, para probar la pantalla
 * "¿Por qué este número?" sin conexión. Corre después de DemoData (ApplicationReadyEvent).
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoExplicaciones {

    private final EdicionRepository edicionRepository;
    private final TitularRepository titularRepository;
    private final ExplicacionRepository explicacionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sembrar() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        edicionRepository.findByFecha(hoy).ifPresent(edicion -> {
            for (Titular titular : titularRepository.findConMercadoByEdicionId(edicion.getId())) {
                if (explicacionRepository.existsByTitularId(titular.getId())) {
                    continue;
                }
                LocalDateTime ahora = LocalDateTime.now();
                explicacionRepository.save(Explicacion.builder()
                        .titular(titular)
                        .resumen("Ejemplo de demostración: en este modo no se consultan fuentes reales. "
                                + "En producción aquí aparece un resumen de las noticias [1] y los comentarios [2] "
                                + "que coinciden con el movimiento del mercado.")
                        .fuentes(new ArrayList<>(List.of(
                                FuenteExplicacion.builder()
                                        .tipo(TipoFuente.NOTICIA)
                                        .titulo("Nota de ejemplo para el modo demostración")
                                        .medio("Fuente de ejemplo")
                                        .fecha(ahora)
                                        .build(),
                                FuenteExplicacion.builder()
                                        .tipo(TipoFuente.COMENTARIO)
                                        .titulo("Comentario de ejemplo de un participante del mercado.")
                                        .medio("Comentario de ejemplo")
                                        .fecha(ahora)
                                        .build())))
                        .generadaEn(ahora)
                        .build());
            }
        });
    }
}
