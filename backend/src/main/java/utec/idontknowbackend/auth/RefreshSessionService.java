package utec.idontknowbackend.auth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.exceptions.InvalidCredentialsException;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Service @RequiredArgsConstructor
public class RefreshSessionService {
 private final RefreshSessionRepository sessions;
 private final UsuarioRepository users;
 private final JwtService jwt;
 private final SecureRandom random=new SecureRandom();
 @Transactional
 public String issue(String email) {
  byte[] bytes=new byte[32];random.nextBytes(bytes);
  String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  RefreshSession session=new RefreshSession();session.setTokenHash(hash(token));session.setEmail(email);session.setExpiresAt(Instant.now().plus(30,ChronoUnit.DAYS));
  sessions.save(session);return token;
 }
 @Transactional
 public AuthResponseDTO rotate(String token) {
  if(token==null||token.isBlank())throw invalid();
  RefreshSession session=sessions.findLocked(hash(token)).orElseThrow(this::invalid);
  if(session.isRevoked()||!session.getExpiresAt().isAfter(Instant.now()))throw invalid();
  var user=users.findByEmail(session.getEmail()).orElseThrow(this::invalid);
  session.setRevoked(true);sessions.save(session);
  return AuthResponseDTO.builder().token(jwt.generateToken(user.getEmail())).nombre(user.getNombre()).email(user.getEmail()).role(user.getRole().name()).refreshToken(issue(user.getEmail())).build();
 }
 @Transactional
 public void revoke(String token) {
  if(token!=null&&!token.isBlank())sessions.findLocked(hash(token)).ifPresent(s->{s.setRevoked(true);sessions.save(s);});
 }
 private InvalidCredentialsException invalid(){return new InvalidCredentialsException("La sesión expiró. Inicia sesión nuevamente.");}
 private String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
