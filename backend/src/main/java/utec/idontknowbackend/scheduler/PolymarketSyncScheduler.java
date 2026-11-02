package utec.idontknowbackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import utec.idontknowbackend.edicion.model.PortadaBuilderService;
import utec.idontknowbackend.polymarket.MovimientoMercado;
import utec.idontknowbackend.polymarket.PolymarketIngestService;

import java.util.List;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="sync.enabled", havingValue="true", matchIfMissing=true)
@Component
@RequiredArgsConstructor
@Slf4j
public class PolymarketSyncScheduler {

    private final PolymarketIngestService ingestService;
    private final PortadaBuilderService portadaBuilderService;

    @Async
    @Scheduled(cron = "0 0 6 * * *", zone = "America/Lima") // todos los días 6:00 a.m.
    public void sincronizarYGenerarPortada() {
        log.info("Iniciando sincronización diaria con Polymarket...");
        List<MovimientoMercado> movimientos = ingestService.sincronizar();
        log.info("{} mercados sincronizados, armando portada...", movimientos.size());
        portadaBuilderService.construir(movimientos);
        log.info("Portada del día generada correctamente.");
    }
}
