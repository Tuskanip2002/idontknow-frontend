package utec.idontknowbackend.config;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import java.util.*;
@RestController @RequestMapping("/estado") @RequiredArgsConstructor
public class StatusController {
 private final Environment environment;private final DataSource dataSource;
 @GetMapping public Map<String,Object> status() throws java.sql.SQLException {
  try(var connection=dataSource.getConnection()){if(!connection.isValid(3))throw new java.sql.SQLException("Database unavailable");}
  return Map.of("status","ok","demo",Arrays.asList(environment.getActiveProfiles()).contains("demo"));
 }
}
