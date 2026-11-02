package utec.idontknowbackend.polymarket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class PolymarketMarketDTO {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String id;
    private String question;
    private String outcomes;       // JSON-encoded string: "[\"Yes\",\"No\"]"
    private String outcomePrices;  // JSON-encoded string: "[\"0.62\",\"0.38\"]"
    private String endDate;        // ISO-8601
    private Boolean active;
    private Boolean closed;

    /** Devuelve la probabilidad del outcome "Yes", o null si no se puede leer. */
    public BigDecimal getProbabilidadSi() {
        try {
            List<String> nombres = MAPPER.readValue(outcomes, List.class);
            List<String> precios = MAPPER.readValue(outcomePrices, List.class);
            int idx = nombres.indexOf("Yes");
            if (idx == -1 || idx >= precios.size()) return null;
            return new BigDecimal(precios.get(idx));
        } catch (JsonProcessingException | NumberFormatException ex) {
            return null;
        }
    }

    public LocalDateTime getEndDateAsLocalDateTime() {
        if (endDate == null) return null;
        try {
            return OffsetDateTime.parse(endDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (Exception ex) {
            return null;
        }
    }
}
