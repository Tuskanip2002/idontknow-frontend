// guardado/infrastructure/GuardadoRepository.java
package utec.idontknowbackend.guardado.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.guardado.model.Guardado;

import java.util.List;
import java.util.Optional;

public interface GuardadoRepository extends JpaRepository<Guardado, Long> {
    List<Guardado> findByUsuarioId(Long usuarioId);
    Optional<Guardado> findByUsuarioIdAndTitularId(Long usuarioId, Long titularId);
}
