package utec.idontknowbackend.snapshot.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.snapshot.infrastructure.SnapshotRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotRepository snapshotRepository;

    // si el job corre dos veces el mismo día se actualiza la foto de hoy en vez de crear otra
    public void registrar(Mercado mercado, BigDecimal probabilidad) {
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"));
        Snapshot snapshot = snapshotRepository.findByMercadoIdAndFecha(mercado.getId(), hoy)
                .orElseGet(() -> Snapshot.builder().mercado(mercado).fecha(hoy).build());

        snapshot.setProbabilidad(probabilidad);
        snapshotRepository.save(snapshot);
    }

    public Optional<BigDecimal> probabilidadAnterior(Long mercadoId) {
        return snapshotRepository.findFirstByMercadoIdAndFechaBeforeOrderByFechaDesc(mercadoId, LocalDate.now(java.time.ZoneId.of("America/Lima")))
                .map(Snapshot::getProbabilidad);
    }
}

