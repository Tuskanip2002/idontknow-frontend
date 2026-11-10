package utec.idontknowbackend.edicion.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.polymarket.GroqTraduccionClient;
import utec.idontknowbackend.polymarket.MovimientoMercado;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.titular.model.Titular;
import utec.idontknowbackend.util.ScoreCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortadaBuilderService {

    private static final int TITULARES_POR_EDICION = 10;
    private static final BigDecimal UMBRAL = new BigDecimal("0.50");

    private final EdicionRepository edicionRepository;
    private final TitularRepository titularRepository;
    private final GroqTraduccionClient groqClient;
    private final ApplicationEventPublisher eventPublisher;

    public void construir(List<MovimientoMercado> movimientos) {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));

        List<MovimientoMercado> top = movimientos.stream()
                .sorted(Comparator.comparingDouble(this::calcularPuntaje).reversed())
                .limit(TITULARES_POR_EDICION)
                .toList();

        if (top.isEmpty()) {
            log.info("No hay movimientos suficientes para armar la portada de {}", hoy);
            return;
        }

        Edicion edicion = edicionRepository.findByFecha(hoy)
                .orElseGet(() -> edicionRepository.save(Edicion.builder().fecha(hoy).build()));

        for (MovimientoMercado mov : top) {
            titularRepository.findByEdicionIdAndMercadoId(edicion.getId(), mov.mercado().getId())
                    .ifPresentOrElse(
                            existente -> actualizarTitular(existente, mov),
                            () -> crearTitular(edicion, mov));
        }

        long cantidad = titularRepository.countByEdicionId(edicion.getId());
        eventPublisher.publishEvent(new PortadaGeneradaEvent(this, edicion, cantidad));
    }

    // si el job vuelve a correr el mismo día no se duplica el titular, solo se actualizan los números
    private void actualizarTitular(Titular titular, MovimientoMercado mov) {
        if (mov.mercado().getPreguntaOriginal().equals(titular.getTextoEspanol())) {
            titular.setTextoEspanol(groqClient.traducir(mov.mercado().getPreguntaOriginal(), mov.nueva()));
        }
        titular.setProbabilidadHoy(mov.nueva());
        titular.setCambioDesdeAyer(mov.nueva().subtract(mov.anterior()));
        titular.setPuntaje(calcularPuntaje(mov));
        titularRepository.save(titular);
    }

    private void crearTitular(Edicion edicion, MovimientoMercado mov) {
        if (titularRepository.countByEdicionId(edicion.getId()) >= TITULARES_POR_EDICION) return;

        String textoEspanol = groqClient.traducir(mov.mercado().getPreguntaOriginal(), mov.nueva());

        Titular titular = Titular.builder()
                .edicion(edicion)
                .mercado(mov.mercado())
                .textoEspanol(textoEspanol)
                .probabilidadHoy(mov.nueva())
                .cambioDesdeAyer(mov.nueva().subtract(mov.anterior()))
                .puntaje(calcularPuntaje(mov))
                .build();

        titularRepository.save(titular);
    }

    private double calcularPuntaje(MovimientoMercado mov) {
        boolean cruzoUmbral = mov.anterior().compareTo(UMBRAL) < 0 && mov.nueva().compareTo(UMBRAL) >= 0;
        boolean seResuelvePronto = mov.mercado().getFechaResolucionEstimada() != null
                && mov.mercado().getFechaResolucionEstimada().isBefore(LocalDateTime.now().plusDays(7));

        return ScoreCalculator.calcular(mov.nueva().subtract(mov.anterior()), cruzoUmbral, seResuelvePronto);
    }
}
