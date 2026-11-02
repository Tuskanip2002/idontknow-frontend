package utec.idontknowbackend.polymarket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.mercado.model.MercadoService;
import utec.idontknowbackend.snapshot.model.SnapshotService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolymarketIngestService {

    private final PolymarketClient polymarketClient;
    private final MercadoRepository mercadoRepository;
    private final MercadoService mercadoService;
    private final SnapshotService snapshotService;
    private final CategoriaClasificador clasificador;
    private final TransactionTemplate transactionTemplate;

    public List<MovimientoMercado> sincronizar() {
        List<PolymarketMarketDTO> mercadosExternos = polymarketClient.obtenerMercadosActivos();
        List<MovimientoMercado> movimientos = new ArrayList<>();

        for (PolymarketMarketDTO dto : mercadosExternos) {
            BigDecimal nuevaProbabilidad = dto.getProbabilidadSi();
            if (nuevaProbabilidad == null) continue;

            try {
                // una transacción por mercado: si uno falla no se cae toda la sincronización
                MovimientoMercado mov = transactionTemplate.execute(status -> procesar(dto, nuevaProbabilidad));
                movimientos.add(mov);
            } catch (Exception ex) {
                log.error("Error sincronizando mercado {}: {}", dto.getId(), ex.getMessage());
            }
        }
        return movimientos;
    }

    private MovimientoMercado procesar(PolymarketMarketDTO dto, BigDecimal nuevaProbabilidad) {
        Mercado mercado = mercadoRepository.findByPolymarketId(dto.getId())
                .orElseGet(() -> crearMercado(dto, nuevaProbabilidad));

        if (mercado.getCategorias().isEmpty()) {
            mercado.getCategorias().addAll(clasificador.clasificar(mercado.getPreguntaOriginal()));
        }

        // se compara contra la última foto de un día anterior; si es nuevo no hay cambio
        BigDecimal anterior = snapshotService.probabilidadAnterior(mercado.getId()).orElse(nuevaProbabilidad);

        mercadoService.actualizarProbabilidad(mercado, nuevaProbabilidad);
        snapshotService.registrar(mercado, nuevaProbabilidad);

        return new MovimientoMercado(mercado, anterior, nuevaProbabilidad);
    }

    private Mercado crearMercado(PolymarketMarketDTO dto, BigDecimal probabilidad) {
        Mercado nuevo = Mercado.builder()
                .polymarketId(dto.getId())
                .preguntaOriginal(dto.getQuestion())
                .probabilidadActual(probabilidad)
                .fechaResolucionEstimada(dto.getEndDateAsLocalDateTime())
                .resuelto(false)
                .build();
        return mercadoRepository.save(nuevo);
    }
}

