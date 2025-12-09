package site.dogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DogetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogetherApplication.class, args);
    }
}
