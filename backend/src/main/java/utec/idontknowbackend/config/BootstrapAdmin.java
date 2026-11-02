package utec.idontknowbackend.config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.*;
@Component @Profile("!demo") @RequiredArgsConstructor
public class BootstrapAdmin implements CommandLineRunner {
 private final UsuarioRepository users;private final PasswordEncoder encoder;
 @Value("${ADMIN_EMAIL:}") private String email;
 @Value("${ADMIN_PASSWORD:}") private String password;
 @Override @Transactional public void run(String...args){
  if(email.isBlank()||password.isBlank()||users.existsByEmail(email))return;
  if(password.length()<12||!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new IllegalStateException("Configura un correo y una contraseña de administrador válidos (12 caracteres, mayúscula, minúscula y número).");
  users.save(Usuario.builder().nombre("Administrador").email(email).password(encoder.encode(password)).role(Role.ADMIN).build());
 }
}
