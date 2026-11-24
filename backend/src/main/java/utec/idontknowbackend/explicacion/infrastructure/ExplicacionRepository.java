package utec.idontknowbackend.explicacion.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.explicacion.model.Explicacion;

import java.util.Optional;

public interface ExplicacionRepository extends JpaRepository<Explicacion, Long> {
    Optional<Explicacion> findByTitularId(Long titularId);
    boolean existsByTitularId(Long titularId);
}
