package utec.idontknowbackend.auth;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity
@Table(name="refresh_sessions")
@Getter @Setter
public class RefreshSession {
 @Id @Column(length=64) private String tokenHash;
 @Column(nullable=false) private String email;
 @Column(nullable=false) private Instant expiresAt;
 private boolean revoked;
}
