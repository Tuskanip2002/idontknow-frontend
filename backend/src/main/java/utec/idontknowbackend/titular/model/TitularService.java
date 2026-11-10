package utec.idontknowbackend.titular.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.snapshot.DTO.SnapshotResponseDTO;
import utec.idontknowbackend.snapshot.infrastructure.SnapshotRepository;
import utec.idontknowbackend.titular.DTO.TitularDetailDTO;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;

@org.springframework.transaction.annotation.Transactional
@Service
@RequiredArgsConstructor
public class TitularService {

    private final TitularRepository titularRepository;
    private final SnapshotRepository snapshotRepository;
    private final ModelMapper modelMapper;

    public TitularDetailDTO getDetalle(Long id) {
        Titular titular = titularRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Titular no encontrado"));

        TitularDetailDTO dto = modelMapper.map(titular, TitularDetailDTO.class);
        dto.setMercadoId(titular.getMercado().getId());
        dto.setFechaResolucionEstimada(titular.getMercado().getFechaResolucionEstimada());
        dto.setHistorial(
                snapshotRepository.findByMercadoIdOrderByFechaAsc(titular.getMercado().getId()).stream()
                        .map(s -> modelMapper.map(s, SnapshotResponseDTO.class))
                        .toList()
        );
        return dto;
    }
}
