package utec.idontknowbackend.auth;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface RefreshSessionRepository extends JpaRepository<RefreshSession,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select s from RefreshSession s where s.tokenHash = :hash")
 Optional<RefreshSession> findLocked(@Param("hash") String hash);
}
