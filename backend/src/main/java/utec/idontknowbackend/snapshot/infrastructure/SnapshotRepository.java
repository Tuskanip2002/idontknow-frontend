// snapshot/infrastructure/SnapshotRepository.java
package utec.idontknowbackend.snapshot.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.snapshot.model.Snapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {
    List<Snapshot> findByMercadoIdOrderByFechaAsc(Long mercadoId);
    Optional<Snapshot> findByMercadoIdAndFecha(Long mercadoId, LocalDate fecha);
    Optional<Snapshot> findFirstByMercadoIdAndFechaBeforeOrderByFechaDesc(Long mercadoId, LocalDate fecha);
}

