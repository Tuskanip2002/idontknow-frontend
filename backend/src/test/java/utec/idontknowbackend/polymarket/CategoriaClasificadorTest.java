package utec.idontknowbackend.polymarket;

import org.junit.jupiter.api.Test;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.categoria.model.Categoria;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CategoriaClasificadorTest {
    @Test
    void clasificaLideresYNominacionesPoliticas() {
        CategoriaRepository repository = mock(CategoriaRepository.class);
        Categoria politica = new Categoria();
        when(repository.findByNombre("POLITICA")).thenReturn(Optional.of(politica));
        CategoriaClasificador clasificador = new CategoriaClasificador(repository);
        assertTrue(clasificador.clasificar("Xi Jinping out before 2027?").contains(politica));
        assertTrue(clasificador.clasificar("Who wins the Democratic nomination?").contains(politica));
    }
}
