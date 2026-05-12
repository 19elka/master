package task1.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> weatherProducerConfigs() {
        return Map.ofEntries(
                Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName())
        );
    }

    @Bean
    public ProducerFactory<String, String> weatherProducerFactory() {
        return new DefaultKafkaProducerFactory<>(
                weatherProducerConfigs(),
                new StringSerializer(),
                new StringSerializer()
        );
    }

    @Bean(name = "weatherKafkaTemplate")
    @Primary
    public KafkaTemplate<String, String> weatherKafkaTemplate(
            @Qualifier("weatherProducerFactory") ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}