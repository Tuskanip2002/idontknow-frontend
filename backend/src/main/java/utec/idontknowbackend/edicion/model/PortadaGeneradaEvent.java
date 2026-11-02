package utec.idontknowbackend.edicion.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class PortadaGeneradaEvent extends ApplicationEvent {

    private final Long edicionId;
    private final LocalDate fecha;
    private final long cantidadTitulares;

    public PortadaGeneradaEvent(Object source, Edicion edicion, long cantidadTitulares) {
        super(source);
        this.edicionId = edicion.getId();
        this.fecha = edicion.getFecha();
        this.cantidadTitulares = cantidadTitulares;
    }
}

