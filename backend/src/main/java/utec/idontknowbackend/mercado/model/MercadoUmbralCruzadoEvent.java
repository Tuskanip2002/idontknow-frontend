package utec.idontknowbackend.mercado.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class MercadoUmbralCruzadoEvent extends ApplicationEvent {

    // se pasan datos planos y no la entidad, porque el listener corre en otro hilo sin sesión de Hibernate
    private final Long mercadoId;
    private final String pregunta;
    private final BigDecimal probabilidad;

    public MercadoUmbralCruzadoEvent(Object source, Mercado mercado) {
        super(source);
        this.mercadoId = mercado.getId();
        this.pregunta = mercado.getPreguntaOriginal();
        this.probabilidad = mercado.getProbabilidadActual();
    }
}

