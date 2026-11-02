package utec.idontknowbackend.user.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UsuarioRegistradoEvent extends ApplicationEvent {

    private final Usuario usuario;

    public UsuarioRegistradoEvent(Object source, Usuario usuario) {
        super(source);
        this.usuario = usuario;
    }
}
