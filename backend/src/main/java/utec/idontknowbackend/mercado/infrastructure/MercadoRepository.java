package utec.idontknowbackend.mercado.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.idontknowbackend.mercado.model.Mercado;

import java.util.Optional;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {
    Optional<Mercado> findByPolymarketId(String polymarketId);

    @Query("""
            select distinct m from Mercado m
            left join m.categorias c
            where (:categoria = '' or upper(c.nombre) = upper(:categoria))
            and upper(m.preguntaOriginal) like upper(concat('%', :q, '%'))
            """)
    Page<Mercado> buscar(@Param("categoria") String categoria, @Param("q") String q, Pageable pageable);
}
