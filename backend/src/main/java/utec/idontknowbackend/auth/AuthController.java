package utec.idontknowbackend.auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {
 private final AuthService authService;
 private final RefreshSessionService sessions;
 @Value("${session.cookie-secure:true}") private boolean cookieSecure;
 @PostMapping("/register")
 public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto,@RequestHeader(value="X-Client-Type",defaultValue="web") String platform){
  AuthResponseDTO response=authService.register(dto);response.setRefreshToken(sessions.issue(response.getEmail()));return respond(response,platform,HttpStatus.CREATED);
 }
 @PostMapping("/login")
 public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto,@RequestHeader(value="X-Client-Type",defaultValue="web") String platform){
  AuthResponseDTO response=authService.login(dto);response.setRefreshToken(sessions.issue(response.getEmail()));return respond(response,platform,HttpStatus.OK);
 }
 @PostMapping("/refresh")
 public ResponseEntity<AuthResponseDTO> refresh(@RequestHeader("X-Client-Type") String platform,@CookieValue(value="idk_refresh",required=false) String cookie,@RequestBody(required=false) RefreshRequestDTO dto){
  return respond(sessions.rotate(token(platform,cookie,dto)),platform,HttpStatus.OK);
 }
 @PostMapping("/logout")
 public ResponseEntity<Void> logout(@RequestHeader("X-Client-Type") String platform,@CookieValue(value="idk_refresh",required=false) String cookie,@RequestBody(required=false) RefreshRequestDTO dto){
  sessions.revoke(token(platform,cookie,dto));return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,cookie("",Duration.ZERO).toString()).header(HttpHeaders.CACHE_CONTROL,"no-store").build();
 }
 private String token(String platform,String cookie,RefreshRequestDTO dto){return "mobile".equals(platform)?(dto==null?null:dto.refreshToken()):cookie;}
 private ResponseEntity<AuthResponseDTO> respond(AuthResponseDTO response,String platform,HttpStatus status){
  var builder=ResponseEntity.status(status).header(HttpHeaders.CACHE_CONTROL,"no-store");
  if(!"mobile".equals(platform)){builder.header(HttpHeaders.SET_COOKIE,cookie(response.getRefreshToken(),Duration.ofDays(30)).toString());response.setRefreshToken(null);}
  return builder.body(response);
 }
 private ResponseCookie cookie(String value,Duration age){return ResponseCookie.from("idk_refresh",value).httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/api/v1/auth").maxAge(age).build();}
}
