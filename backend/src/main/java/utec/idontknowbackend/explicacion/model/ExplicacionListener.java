package utec.idontknowbackend.explicacion.model;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import utec.idontknowbackend.edicion.model.PortadaGeneradaEvent;

/** Dispara la generación de explicaciones. Se apaga con explicaciones.enabled=false (perfil demo). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "explicaciones.enabled", havingValue = "true", matchIfMissing = true)
public class ExplicacionListener {

    private final ExplicacionGenerador generador;

    // fallbackExecution: la portada puede publicarse fuera de una transacción
    @TransactionalEventListener(fallbackExecution = true)
    public void onPortadaGenerada(PortadaGeneradaEvent event) {
        generador.generarEnSegundoPlano(event.getEdicionId(), false);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        generador.generarHoySiFalta();
    }
}
