package utec.idontknowbackend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import utec.idontknowbackend.edicion.model.PortadaGeneradaEvent;
import utec.idontknowbackend.exceptions.EmailSendingException;
import utec.idontknowbackend.mercado.model.MercadoUmbralCruzadoEvent;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.Usuario;
import utec.idontknowbackend.user.model.UsuarioRegistradoEvent;

import java.util.List;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="email.enabled", havingValue="true", matchIfMissing=true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UsuarioRepository usuarioRepository;

    @Async
    @EventListener
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        Usuario usuario = event.getUsuario();
        Context context = new Context();
        context.setVariable("nombre", usuario.getNombre());
        enviar(usuario.getEmail(), "¡Bienvenido a I don't know!", "welcome-email", context);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMercadoUmbralCruzado(MercadoUmbralCruzadoEvent event) {
        List<Usuario> seguidores = usuarioRepository.findSeguidoresDeMercado(event.getMercadoId());

        for (Usuario usuario : seguidores) {
            Context context = new Context();
            context.setVariable("nombre", usuario.getNombre());
            context.setVariable("pregunta", event.getPregunta());
            context.setVariable("probabilidad", event.getProbabilidad());
            try {
                enviar(usuario.getEmail(), "Un mercado que sigues cruzó el 50%", "umbral-cruzado-email", context);
            } catch (Exception ex) {
                log.error("No se pudo avisar a {}: {}", usuario.getEmail(), ex.getMessage());
            }
        }
    }

    @Async
    @EventListener
    public void onPortadaGenerada(PortadaGeneradaEvent event) {
        log.info("Portada generada: {} titulares para el {}", event.getCantidadTitulares(), event.getFecha());
    }

    private void enviar(String destinatario, String asunto, String template, Context context) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            String html = templateEngine.process(template, context);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(mensaje);
        } catch (MessagingException ex) {
            throw new EmailSendingException("No se pudo enviar el correo a " + destinatario, ex);
        }
    }
}
