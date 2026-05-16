package task1.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String weatherGroupId;

    @PostConstruct
    void init() {
        log.info("Kafka bootstrapServers={}", bootstrapServers);
    }

    private Map<String, Object> consumerConfigs(String groupId) {

        Map<String, Object> configs = new HashMap<>();

        configs.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        configs.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        configs.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        configs.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        configs.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        configs.put(
                ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,
                true
        );

        return configs;
    }

    private ConcurrentKafkaListenerContainerFactory<String, String>
    buildFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> weatherConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(weatherGroupId)
        );
    }

    @Primary
    @Bean(name = "weatherKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    weatherKafkaListenerContainerFactory(
            @Qualifier("weatherConsumerFactory")
            ConsumerFactory<String, String> weatherConsumerFactory
    ) {

        return buildFactory(weatherConsumerFactory);
    }

    @Bean(name = "pdfKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> pdfKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "pdf-consumer-group");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, true);

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(configs));
        return factory;
    }
}
