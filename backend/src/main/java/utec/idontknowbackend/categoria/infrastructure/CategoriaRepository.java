// categoria/infrastructure/CategoriaRepository.java
package utec.idontknowbackend.categoria.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.idontknowbackend.categoria.model.Categoria;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);
}
