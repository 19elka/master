package task1.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRolesKafkaListener {

    @KafkaListener(topics = "user-roles",
            containerFactory = "userRolesKafkaListenerContainerFactory")
    public void onMessage(String message) {
        log.info("Received message from Kafka [user-roles]: {}", message);
    }
}