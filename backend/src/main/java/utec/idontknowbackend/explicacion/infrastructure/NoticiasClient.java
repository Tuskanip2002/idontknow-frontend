package utec.idontknowbackend.explicacion.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;

import java.util.List;

/** Busca en GDELT y usa RSS si no se obtuvieron noticias. */
@Component
@RequiredArgsConstructor
@Slf4j
public class NoticiasClient {
    private final GdeltClient gdelt;
    private final NoticiasRssClient rss;

    public List<FuenteExplicacion> buscarNoticias(String consulta, int maximo) {
        if (consulta == null || consulta.isBlank() || maximo <= 0) {
            return List.of();
        }
        List<FuenteExplicacion> noticias = gdelt.buscarNoticias(consulta, maximo);
        if (!noticias.isEmpty()) {
            return noticias;
        }
        log.info("Sin noticias de GDELT para '{}'; se consulta el respaldo RSS", consulta);
        return rss.buscarNoticias(consulta, maximo);
    }
}
