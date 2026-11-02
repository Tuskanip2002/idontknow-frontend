package utec.idontknowbackend;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"demo","test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlatformIntegrationTest {
 @LocalServerPort int port;
 final HttpClient client=HttpClient.newBuilder().build();
 final com.fasterxml.jackson.databind.ObjectMapper mapper=new com.fasterxml.jackson.databind.ObjectMapper();
 static String access;static String refresh;static String cookie;static long headlineId;
 HttpResponse<String> request(String method,String path,String body,Map<String,String> headers)throws Exception{
  var builder=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path)).header("Content-Type","application/json");headers.forEach(builder::header);
  return client.send(builder.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test @Order(1) void publicDataAndPagination()throws Exception{
  var markets=request("GET","/mercados?page=1&size=10",null,Map.of());assertEquals(200,markets.statusCode(),markets.body());var data=mapper.readTree(markets.body());assertEquals(24,data.path("totalElements").asInt());assertEquals(10,data.path("content").size());assertEquals(1,data.path("number").asInt());
  var editions=request("GET","/ediciones?page=1&size=10",null,Map.of());assertEquals(200,editions.statusCode());assertEquals(16,mapper.readTree(editions.body()).path("totalElements").asInt());
  var home=request("GET","/ediciones/hoy",null,Map.of());assertEquals(200,home.statusCode(),home.body());headlineId=mapper.readTree(home.body()).path("titulares").get(0).path("id").asLong();assertTrue(headlineId>0);
  assertEquals(401,request("GET","/usuarios/me",null,Map.of()).statusCode());
 }
 @Test @Order(2) void mobileAuthenticationAndSavedFlow()throws Exception{
  var login=request("POST","/auth/login","{\"email\":\"lector@demo.local\",\"password\":\"LectorDemo123\"}",Map.of("X-Client-Type","mobile"));assertEquals(200,login.statusCode(),login.body());
  var data=mapper.readTree(login.body());access=data.path("token").asText();refresh=data.path("refreshToken").asText();assertFalse(refresh.isBlank());
  var headers=Map.of("Authorization","Bearer "+access);
  assertEquals(200,request("GET","/usuarios/me",null,headers).statusCode());
  assertEquals(201,request("POST","/guardados","{\"titularId\":"+headlineId+"}",headers).statusCode());
  assertEquals(409,request("POST","/guardados","{\"titularId\":"+headlineId+"}",headers).statusCode());
  assertEquals(1,mapper.readTree(request("GET","/guardados",null,headers).body()).size());
  assertEquals(204,request("DELETE","/guardados/"+headlineId,null,headers).statusCode());
  assertEquals(403,request("POST","/categorias","{\"nombre\":\"NO_PERMITIDA\"}",headers).statusCode());
 }
 @Test @Order(3) void refreshRotationAndRevocation()throws Exception{
  var first=request("POST","/auth/refresh","{\"refreshToken\":\""+refresh+"\"}",Map.of("X-Client-Type","mobile"));assertEquals(200,first.statusCode(),first.body());
  var rotated=mapper.readTree(first.body()).path("refreshToken").asText();assertNotEquals(refresh,rotated);
  assertEquals(401,request("POST","/auth/refresh","{\"refreshToken\":\""+refresh+"\"}",Map.of("X-Client-Type","mobile")).statusCode());
  assertEquals(204,request("POST","/auth/logout","{\"refreshToken\":\""+rotated+"\"}",Map.of("X-Client-Type","mobile")).statusCode());
  assertEquals(401,request("POST","/auth/refresh","{\"refreshToken\":\""+rotated+"\"}",Map.of("X-Client-Type","mobile")).statusCode());
 }
 @Test @Order(4) void browserSessionUsesHttpOnlyCookie()throws Exception{
  var login=request("POST","/auth/login","{\"email\":\"lector@demo.local\",\"password\":\"LectorDemo123\"}",Map.of("X-Client-Type","web"));assertEquals(200,login.statusCode());
  var setCookie=login.headers().firstValue("set-cookie").orElseThrow();assertTrue(setCookie.contains("HttpOnly"));assertTrue(setCookie.contains("SameSite=Lax"));assertTrue(mapper.readTree(login.body()).path("refreshToken").isMissingNode());
  cookie=setCookie.split(";")[0];var fresh=request("POST","/auth/refresh","{}",Map.of("X-Client-Type","web","Cookie",cookie));assertEquals(200,fresh.statusCode(),fresh.body());
 }

 @Test @Order(5) void registrationProfileAndTopicsPersist()throws Exception{
  String body="{\"nombre\":\"Lector de integración\",\"email\":\"integration@demo.local\",\"password\":\"Integration123\"}";
  var registered=request("POST","/auth/register",body,Map.of("X-Client-Type","mobile"));
  assertEquals(201,registered.statusCode(),registered.body());
  assertEquals(409,request("POST","/auth/register",body,Map.of("X-Client-Type","mobile")).statusCode());
  String token=mapper.readTree(registered.body()).path("token").asText();
  var headers=Map.of("Authorization","Bearer "+token);
  var categories=mapper.readTree(request("GET","/categorias",null,Map.of()).body());
  long categoryId=categories.get(0).path("id").asLong();
  assertEquals(200,request("POST","/usuarios/me/categorias/"+categoryId,"{}",headers).statusCode());
  assertEquals(1,mapper.readTree(request("GET","/usuarios/me",null,headers).body()).path("categoriasSeguidas").size());
  assertEquals(204,request("DELETE","/usuarios/me/categorias/"+categoryId,null,headers).statusCode());
  assertEquals(0,mapper.readTree(request("GET","/usuarios/me",null,headers).body()).path("categoriasSeguidas").size());
  assertEquals(200,request("PUT","/usuarios/me","{\"nombre\":\"Nombre actualizado\"}",headers).statusCode());
  assertEquals("Nombre actualizado",mapper.readTree(request("GET","/usuarios/me",null,headers).body()).path("nombre").asText());
  assertEquals(400,request("POST","/auth/register","{\"email\":\"invalid\"}",Map.of()).statusCode());
 }
 @Test @Order(6) void administratorCanManageCategories()throws Exception{
  var login=request("POST","/auth/login","{\"email\":\"admin@demo.local\",\"password\":\"AdminDemo123\"}",Map.of("X-Client-Type","mobile"));
  assertEquals(200,login.statusCode(),login.body());
  String token=mapper.readTree(login.body()).path("token").asText();
  var headers=Map.of("Authorization","Bearer "+token);
  var created=request("POST","/categorias","{\"nombre\":\"CATEGORIA_INTEGRACION\"}",headers);
  assertEquals(201,created.statusCode(),created.body());
  long id=mapper.readTree(created.body()).path("id").asLong();
  assertEquals(204,request("DELETE","/categorias/"+id,null,headers).statusCode());
  assertEquals(404,request("GET","/mercados/999999",null,Map.of()).statusCode());
 }

}
