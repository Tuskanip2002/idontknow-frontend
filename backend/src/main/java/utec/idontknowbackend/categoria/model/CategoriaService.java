package utec.idontknowbackend.categoria.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.categoria.DTO.CategoriaRequestDTO;
import utec.idontknowbackend.categoria.DTO.CategoriaResponseDTO;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.exceptions.DuplicateResourceException;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;

import java.util.List;

@org.springframework.transaction.annotation.Transactional
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    public List<CategoriaResponseDTO> getAll() {
        return categoriaRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CategoriaResponseDTO.class))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponseDTO create(CategoriaRequestDTO dto) {
        categoriaRepository.findByNombre(dto.getNombre()).ifPresent(c -> {
            throw new DuplicateResourceException("Ya existe la categoría " + dto.getNombre());
        });

        Categoria categoria = modelMapper.map(dto, Categoria.class);
        return modelMapper.map(categoriaRepository.save(categoria), CategoriaResponseDTO.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada");
        }
        categoriaRepository.deleteById(id);
    }
}
