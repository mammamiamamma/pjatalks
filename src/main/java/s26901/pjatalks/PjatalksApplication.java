package s26901.pjatalks;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PjatalksApplication {
    public static void main(String[] args) {
        SpringApplication.run(PjatalksApplication.class, args);
    }
}
