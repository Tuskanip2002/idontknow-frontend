// titular/infrastructure/TitularRepository.java
package utec.idontknowbackend.titular.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.titular.model.Titular;

import java.util.Optional;

public interface TitularRepository extends JpaRepository<Titular, Long> {
    Optional<Titular> findByEdicionIdAndMercadoId(Long edicionId, Long mercadoId);
    long countByEdicionId(Long edicionId);
}

