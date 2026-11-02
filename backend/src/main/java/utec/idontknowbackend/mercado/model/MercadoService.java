package utec.idontknowbackend.mercado.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utec.idontknowbackend.categoria.model.Categoria;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.mercado.DTO.MercadoDetailDTO;
import utec.idontknowbackend.mercado.DTO.MercadoResponseDTO;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.snapshot.DTO.SnapshotResponseDTO;
import utec.idontknowbackend.snapshot.infrastructure.SnapshotRepository;

import java.math.BigDecimal;

@org.springframework.transaction.annotation.Transactional
@Service
@RequiredArgsConstructor
public class MercadoService {

    private static final BigDecimal UMBRAL = new BigDecimal("0.50");

    private final MercadoRepository mercadoRepository;
    private final SnapshotRepository snapshotRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Page<MercadoResponseDTO> getAll(String categoria, String q, Pageable pageable) {
        return mercadoRepository.buscar(categoria, q, pageable)
                .map(this::toResponseDTO);
    }

    public MercadoDetailDTO getDetalle(Long id) {
        Mercado mercado = mercadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mercado no encontrado"));

        MercadoDetailDTO dto = modelMapper.map(mercado, MercadoDetailDTO.class);
        dto.setCategorias(mercado.getCategorias().stream().map(Categoria::getNombre).toList());
        dto.setHistorial(
                snapshotRepository.findByMercadoIdOrderByFechaAsc(id).stream()
                        .map(s -> modelMapper.map(s, SnapshotResponseDTO.class))
                        .toList()
        );
        return dto;
    }

    // usado por el proceso de sincronización diaria con Polymarket (siguiente etapa)
    public void actualizarProbabilidad(Mercado mercado, BigDecimal nuevaProbabilidad) {
        BigDecimal anterior = mercado.getProbabilidadActual();
        mercado.setProbabilidadActual(nuevaProbabilidad);
        mercadoRepository.save(mercado);

        boolean cruzoUmbral = anterior.compareTo(UMBRAL) < 0 && nuevaProbabilidad.compareTo(UMBRAL) >= 0;
        if (cruzoUmbral) {
            eventPublisher.publishEvent(new MercadoUmbralCruzadoEvent(this, mercado));
        }
    }

    private MercadoResponseDTO toResponseDTO(Mercado mercado) {
        MercadoResponseDTO dto = modelMapper.map(mercado, MercadoResponseDTO.class);
        dto.setCategorias(mercado.getCategorias().stream().map(Categoria::getNombre).toList());
        return dto;
    }
}
