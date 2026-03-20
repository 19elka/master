package task1.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRolesKafkaListener {

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @KafkaListener(topics = "user-roles",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String message) {
        log.info("Received message from Kafka [user-roles]: {}", message);
    }
}
