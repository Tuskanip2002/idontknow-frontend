// titular/infrastructure/TitularRepository.java
package utec.idontknowbackend.titular.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.idontknowbackend.titular.model.Titular;

import java.util.List;
import java.util.Optional;

public interface TitularRepository extends JpaRepository<Titular, Long> {
    Optional<Titular> findByEdicionIdAndMercadoId(Long edicionId, Long mercadoId);
    long countByEdicionId(Long edicionId);

    // trae el mercado en la misma consulta: las explicaciones se generan fuera de una transacción
    @Query("select t from Titular t join fetch t.mercado where t.edicion.id = :edicionId order by t.puntaje desc")
    List<Titular> findConMercadoByEdicionId(@Param("edicionId") Long edicionId);
}
