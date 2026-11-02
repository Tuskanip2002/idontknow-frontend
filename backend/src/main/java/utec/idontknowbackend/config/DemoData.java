package utec.idontknowbackend.config;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import utec.idontknowbackend.categoria.infrastructure.CategoriaRepository;
import utec.idontknowbackend.categoria.model.Categoria;
import utec.idontknowbackend.mercado.infrastructure.MercadoRepository;
import utec.idontknowbackend.mercado.model.Mercado;
import utec.idontknowbackend.edicion.infrastructure.EdicionRepository;
import utec.idontknowbackend.edicion.model.Edicion;
import utec.idontknowbackend.titular.model.Titular;
import utec.idontknowbackend.snapshot.infrastructure.SnapshotRepository;
import utec.idontknowbackend.snapshot.model.Snapshot;
import utec.idontknowbackend.user.infrastructure.UsuarioRepository;
import utec.idontknowbackend.user.model.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
@Component @Profile("demo") @RequiredArgsConstructor
public class DemoData implements CommandLineRunner {
 private final CategoriaRepository categories;
 private final MercadoRepository markets;
 private final EdicionRepository editions;
 private final SnapshotRepository snapshots;
 private final UsuarioRepository users;
 private final PasswordEncoder encoder;
 @Override @Transactional public void run(String... args){
  var today=LocalDate.now(ZoneId.of("America/Lima"));
  var topics=new ArrayList<Categoria>();
  for(String name:List.of("POLITICA","ECONOMIA","DEPORTES","TECNOLOGIA")){
   topics.add(categories.findAll().stream().filter(c->c.getNombre().equals(name)).findFirst().orElseGet(()->categories.save(Categoria.builder().nombre(name).build())));
  }
  if(!users.existsByEmail("lector@demo.local"))users.save(Usuario.builder().nombre("Lector de demostración").email("lector@demo.local").password(encoder.encode("LectorDemo123")).role(Role.USER).build());
  if(!users.existsByEmail("admin@demo.local"))users.save(Usuario.builder().nombre("Administrador de demostración").email("admin@demo.local").password(encoder.encode("AdminDemo123")).role(Role.ADMIN).build());
  String[][] stories={
   {"Will a central bank cut interest rates this quarter?","Los mercados anticipan un nuevo giro en las tasas de interés","1"},
   {"Will a clean energy agreement be signed this year?","Un acuerdo de energía limpia gana terreno en las expectativas","0"},
   {"Will a reusable rocket reach orbit this month?","La próxima misión espacial entra en la cuenta regresiva","3"},
   {"Will a South American team win the international final?","La final internacional cambia de favorito","2"},
   {"Will inflation fall below the annual target?","La inflación vuelve al centro de las expectativas","1"},
   {"Will a semiconductor breakthrough be announced?","La industria de chips espera su próximo gran avance","3"},
   {"Will parliament approve a transport reform?","La reforma del transporte enfrenta una semana decisiva","0"},
   {"Will the league leader retain first place?","La carrera por el campeonato sigue abierta","2"},
   {"Will satellite internet expand to new regions?","La conectividad satelital mira hacia nuevas regiones","3"},
   {"Will employment grow in the quarterly report?","El próximo informe de empleo concentra la atención","1"},
   {"Will an international summit reach an agreement?","La cumbre climática busca un punto de encuentro","0"},
   {"Will a player break the tournament record?","Un récord deportivo podría cambiar de dueño","2"}
  };
  var collection=new ArrayList<Mercado>();
  for(int i=0;i<24;i++){
   final int index=i;var story=stories[i%stories.length];
   var market=markets.findByPolymarketId("demo-"+i).orElseGet(()->markets.save(Mercado.builder().polymarketId("demo-"+index).preguntaOriginal(story[0]+(index>=12?" (escenario alternativo)":"")).probabilidadActual(BigDecimal.valueOf(.22+(index*7%65)/100.0)).fechaResolucionEstimada(today.plusDays(index+5).atStartOfDay()).categorias(new HashSet<>(Set.of(topics.get(Integer.parseInt(story[2]))))).build()));
   collection.add(market);
   var history=snapshots.findByMercadoIdOrderByFechaAsc(market.getId());
   for(int day=0;day<14;day++){
    var date=today.minusDays(day);
    if(history.stream().noneMatch(s->s.getFecha().equals(date))){
     double value=Math.max(.02,Math.min(.98,market.getProbabilidadActual().doubleValue()-day*.008+Math.sin(day+index)*.035));
     snapshots.save(Snapshot.builder().mercado(market).fecha(date).probabilidad(BigDecimal.valueOf(value)).build());
    }
   }
  }
  for(int day=0;day<16;day++){
   var date=today.minusDays(day);if(editions.findByFecha(date).isPresent())continue;
   var edition=Edicion.builder().fecha(date).build();
   for(int i=0;i<8;i++){
    var story=stories[(i+day)%stories.length];var market=collection.get((i+day)%collection.size());
    edition.getTitulares().add(Titular.builder().edicion(edition).mercado(market).textoEspanol(story[1]).probabilidadHoy(market.getProbabilidadActual()).cambioDesdeAyer(BigDecimal.valueOf((i%2==0?1:-1)*(.03+i*.01))).puntaje(90.0-i*7).build());
   }
   editions.save(edition);
  }
 }
}
