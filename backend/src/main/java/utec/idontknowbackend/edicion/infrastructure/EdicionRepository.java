// edicion/infrastructure/EdicionRepository.java
package utec.idontknowbackend.edicion.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.edicion.model.Edicion;

import java.time.LocalDate;
import java.util.Optional;

public interface EdicionRepository extends JpaRepository<Edicion, Long> {
    Optional<Edicion> findByFecha(LocalDate fecha);
}
