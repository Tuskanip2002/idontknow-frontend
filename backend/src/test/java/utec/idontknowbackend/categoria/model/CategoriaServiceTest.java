package utec.idontknowbackend.categoria.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import utec.idontknowbackend.categoria.DTO.CategoriaRequestDTO;
import utec.idontknowbackend.categoria.DTO.CategoriaResponseDTO;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private final ModelMapper modelMapper = new ModelMapper();
    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository, modelMapper);
    }

    @Test
    void getAll_devuelveTodasLasCategoriasMapeadas() {
        Categoria politica = Categoria.builder().id(1L).nombre("POLITICA").build();
        when(categoriaRepository.findAll()).thenReturn(List.of(politica));

        List<CategoriaResponseDTO> resultado = categoriaService.getAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("POLITICA");
    }

    @Test
    void create_conNombreDuplicado_lanzaExcepcion() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("POLITICA");
        when(categoriaRepository.findByNombre("POLITICA"))
                .thenReturn(Optional.of(Categoria.builder().id(1L).nombre("POLITICA").build()));

        assertThatThrownBy(() -> categoriaService.create(dto))
                .isInstanceOf(DuplicateResourceException.class);

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void create_conNombreNuevo_laGuarda() {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("SALUD");
        when(categoriaRepository.findByNombre("SALUD")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        CategoriaResponseDTO resultado = categoriaService.create(dto);

        assertThat(resultado.getId()).isEqualTo(5L);
    }

    @Test
    void delete_conIdInexistente_lanzaExcepcion() {
        when(categoriaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoriaService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
