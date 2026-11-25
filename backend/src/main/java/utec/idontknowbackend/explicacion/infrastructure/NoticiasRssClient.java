package utec.idontknowbackend.explicacion.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import utec.idontknowbackend.explicacion.model.FuenteExplicacion;
import utec.idontknowbackend.explicacion.model.Texto;
import utec.idontknowbackend.explicacion.model.TipoFuente;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Lee solo títulos, enlaces, medios y fechas del RSS de búsqueda de Google News. */
@Component
@Slf4j
public class NoticiasRssClient {
    private static final int MAX_XML = 1_000_000;
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final RestClient restClient;
    private final boolean habilitado;
    private final Clock reloj;

    @Autowired
    public NoticiasRssClient(@Value("${noticias.rss.url}") String url,
                             @Value("${noticias.rss.enabled:true}") boolean habilitado) {
        this(url, habilitado, Clock.systemUTC());
    }

    NoticiasRssClient(String url, boolean habilitado, Clock reloj) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(12));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.habilitado = habilitado;
        this.reloj = reloj;
    }

    public List<FuenteExplicacion> buscarNoticias(String consulta, int maximo) {
        if (!habilitado || consulta == null || consulta.isBlank() || maximo <= 0) {
            return List.of();
        }
        try {
            String body = restClient.get().uri(builder -> builder
                    .queryParam("q", "{consulta}")
                    .queryParam("hl", "en-US").queryParam("gl", "US").queryParam("ceid", "US:en")
                    .build(consulta + " when:3d")).retrieve().body(String.class);
            List<FuenteExplicacion> fuentes = leerArticulos(body, maximo, reloj.instant());
            log.info("RSS de noticias para '{}' devolvió {} artículos válidos", consulta, fuentes.size());
            return fuentes;
        } catch (Exception ex) {
            log.warn("No se pudo consultar el RSS de noticias: {}", ex.getClass().getSimpleName());
            return List.of();
        }
    }

    static List<FuenteExplicacion> leerArticulos(String xml, int maximo, Instant ahora) {
        if (xml == null || xml.isBlank() || xml.length() > MAX_XML || maximo <= 0) {
            return List.of();
        }
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            var documento = builder.parse(new InputSource(new StringReader(xml)));
            var items = documento.getElementsByTagName("item");
            List<FuenteExplicacion> noticias = new ArrayList<>();
            Set<String> titulos = new HashSet<>();
            Set<String> urls = new HashSet<>();
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String medio = texto(item, "source");
                String titulo = texto(item, "title");
                String url = texto(item, "link");
                Instant fecha = fecha(texto(item, "pubDate"));
                if (!medio.isBlank() && titulo.endsWith(" - " + medio)) {
                    titulo = titulo.substring(0, titulo.length() - medio.length() - 3).trim();
                }
                if (titulo.isBlank() || medio.isBlank() || !urlValida(url) || fecha == null
                        || fecha.isBefore(ahora.minus(Duration.ofDays(3)))
                        || fecha.isAfter(ahora.plus(Duration.ofMinutes(5)))
                        || urls.contains(url) || titulos.contains(titulo.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                urls.add(url);
                titulos.add(titulo.toLowerCase(Locale.ROOT));
                noticias.add(FuenteExplicacion.builder().tipo(TipoFuente.NOTICIA)
                        .titulo(Texto.recortar(titulo, 300)).url(url)
                        .medio(Texto.recortar(medio, 200))
                        .fecha(LocalDateTime.ofInstant(fecha, LIMA)).build());
            }
            return noticias.stream().sorted(Comparator.comparing(FuenteExplicacion::getFecha).reversed())
                    .limit(maximo).toList();
        } catch (Exception ex) {
            log.warn("RSS de noticias inválido; no se utilizaron sus artículos ({})", ex.getClass().getSimpleName());
            return List.of();
        }
    }

    private static String texto(Element item, String etiqueta) {
        var nodos = item.getElementsByTagName(etiqueta);
        return nodos.getLength() == 0 ? "" : nodos.item(0).getTextContent().trim();
    }

    private static boolean urlValida(String texto) {
        if (texto.length() > 1000) return false;
        try {
            URI url = URI.create(texto);
            return url.getHost() != null && url.getUserInfo() == null
                    && ("https".equalsIgnoreCase(url.getScheme()) || "http".equalsIgnoreCase(url.getScheme()));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static Instant fecha(String texto) {
        try {
            return ZonedDateTime.parse(texto, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (Exception ex) {
            return null;
        }
    }
}
