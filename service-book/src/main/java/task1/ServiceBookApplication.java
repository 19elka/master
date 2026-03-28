package task1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@ConfigurationPropertiesScan
@EnableConfigurationProperties
@EnableKafka
@SpringBootApplication
public class ServiceBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceBookApplication.class, args);
    }
}