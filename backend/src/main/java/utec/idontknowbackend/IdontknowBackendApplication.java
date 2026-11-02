package utec.idontknowbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdontknowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdontknowBackendApplication.class, args);
    }

}

