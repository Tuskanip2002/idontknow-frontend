package utec.idontknowbackend.auth;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.*;
import utec.idontknowbackend.exceptions.InvalidCredentialsException;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {
 @Mock RefreshSessionRepository sessions;
 @Mock UsuarioRepository users;
 @Mock JwtService jwt;
 @InjectMocks RefreshSessionService service;
 @Test void storesOnlyHashAndRotatesToken(){
  String raw=service.issue("reader@example.com");
  ArgumentCaptor<RefreshSession> captor=ArgumentCaptor.forClass(RefreshSession.class);
  verify(sessions).save(captor.capture());
  var stored=captor.getValue();
  assertNotEquals(raw,stored.getTokenHash());assertEquals(64,stored.getTokenHash().length());
  when(sessions.findLocked(stored.getTokenHash())).thenReturn(Optional.of(stored));
  when(users.findByEmail(stored.getEmail())).thenReturn(Optional.of(Usuario.builder().email(stored.getEmail()).nombre("Reader").role(Role.USER).build()));
  when(jwt.generateToken(stored.getEmail())).thenReturn("new-access");
  var refreshed=service.rotate(raw);
  assertTrue(stored.isRevoked());assertNotEquals(raw,refreshed.getRefreshToken());assertEquals("new-access",refreshed.getToken());
  assertThrows(InvalidCredentialsException.class,()->service.rotate(raw));
 }
 @Test void rejectsExpiredSession(){
  var session=new RefreshSession();session.setExpiresAt(Instant.now().minusSeconds(1));
  when(sessions.findLocked(anyString())).thenReturn(Optional.of(session));
  assertThrows(InvalidCredentialsException.class,()->service.rotate("expired"));
  verifyNoInteractions(users,jwt);
 }
 @Test void logoutRevokesSession(){
  var session=new RefreshSession();when(sessions.findLocked(anyString())).thenReturn(Optional.of(session));
  service.revoke("token");assertTrue(session.isRevoked());verify(sessions).save(session);
 }
}
