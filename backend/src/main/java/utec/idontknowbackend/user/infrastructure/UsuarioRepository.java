// user/infrastructure/UsuarioRepository.java
package utec.idontknowbackend.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.idontknowbackend.user.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("select distinct u from Usuario u join u.categoriasSeguidas c join c.mercados m where m.id = :mercadoId")
    List<Usuario> findSeguidoresDeMercado(@Param("mercadoId") Long mercadoId);
}
