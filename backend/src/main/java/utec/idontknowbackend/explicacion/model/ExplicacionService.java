package utec.idontknowbackend.explicacion.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.edicion.model.Edicion;
import utec.idontknowbackend.exceptions.ResourceNotFoundException;
import utec.idontknowbackend.explicacion.DTO.ExplicacionResponseDTO;
import utec.idontknowbackend.explicacion.DTO.FuenteResponseDTO;
import utec.idontknowbackend.explicacion.infrastructure.ExplicacionRepository;
import utec.idontknowbackend.titular.infrastructure.TitularRepository;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Role;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExplicacionService {

    private final ExplicacionRepository explicacionRepository;
    private final TitularRepository titularRepository;
    private final EdicionRepository edicionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExplicacionGenerador generador;

    @Transactional(readOnly = true)
    public ExplicacionResponseDTO obtener(Long titularId) {
        if (!titularRepository.existsById(titularId)) {
            throw new ResourceNotFoundException("Titular no encontrado");
        }
        boolean desbloqueado = usuarioActualTieneAcceso();

        return explicacionRepository.findByTitularId(titularId)
                .map(explicacion -> toResponseDTO(titularId, explicacion, desbloqueado))
                .orElseGet(() -> ExplicacionResponseDTO.builder()
                        .titularId(titularId)
                        .disponible(false)
                        .bloqueado(!desbloqueado)
                        .cantidadFuentes(0)
                        .tiposFuente(List.of())
                        .fuentes(List.of())
                        .build());
    }

    /** Regenera en segundo plano las explicaciones de la portada de hoy (solo ADMIN). */
    public void regenerarHoy() {
        Edicion edicion = edicionRepository.findByFecha(LocalDate.now(ZoneId.of("America/Lima")))
                .orElseThrow(() -> new ResourceNotFoundException("Todavía no hay portada de hoy"));
        generador.generarEnSegundoPlano(edicion.getId(), false);
    }

    // /titulares/** es público: un visitante sin sesión llega como AnonymousAuthenticationToken
    private boolean usuarioActualTieneAcceso() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return usuarioRepository.findByEmail(auth.getName())
                .map(usuario -> usuario.getRole() == Role.ADMIN || usuario.esPremium())
                .orElse(false);
    }

    private ExplicacionResponseDTO toResponseDTO(Long titularId, Explicacion explicacion, boolean desbloqueado) {
        List<FuenteExplicacion> fuentes = explicacion.getFuentes();
        return ExplicacionResponseDTO.builder()
                .titularId(titularId)
                .disponible(true)
                .bloqueado(!desbloqueado)
                .cantidadFuentes(fuentes.size())
                .tiposFuente(fuentes.stream().map(f -> f.getTipo().name()).distinct().toList())
                .resumen(desbloqueado ? explicacion.getResumen() : null)
                .fuentes(desbloqueado ? fuentes.stream().map(this::toFuenteDTO).toList() : List.of())
                .generadaEn(explicacion.getGeneradaEn())
                .build();
    }

    private FuenteResponseDTO toFuenteDTO(FuenteExplicacion fuente) {
        return FuenteResponseDTO.builder()
                .tipo(fuente.getTipo().name())
                .titulo(fuente.getTitulo())
                .url(fuente.getUrl())
                .medio(fuente.getMedio())
                .fecha(fuente.getFecha())
                .build();
    }
}
