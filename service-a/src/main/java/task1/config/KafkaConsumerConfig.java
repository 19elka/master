package task1.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:host.minikube.internal:29092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:weather-consumer}")
    private String groupId;

    @PostConstruct
    void init() {
        log.info("KafkaConsumerConfig init, bootstrapServers={}", bootstrapServers);
    }

    @Bean
    public Map<String, Object> weatherConsumerConfigs() {
        return Map.ofEntries(
                Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()),
                Map.entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()),
                Map.entry(ConsumerConfig.GROUP_ID_CONFIG, groupId),
                Map.entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"));
    }

    @Bean
    public ConsumerFactory<String, String> weatherConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                weatherConsumerConfigs(),
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean(name = "weatherKafkaListenerContainerFactory")
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> weatherKafkaListenerContainerFactory( ConsumerFactory<String, String> weatherConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(weatherConsumerFactory);
        return factory;
    }
}
