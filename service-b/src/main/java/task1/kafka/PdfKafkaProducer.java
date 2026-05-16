package task1.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfKafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private static final String TOPIC = "pdf-documents";

    public void sendPdfFile(byte[] data, String fileName, String contentType, String documentId) {
        try {
            Message<byte[]> message = MessageBuilder
                    .withPayload(data)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(KafkaHeaders.KEY, documentId)
                    .setHeader("document-id", documentId)
                    .setHeader("file-name", fileName)
                    .setHeader("content-type", contentType)
                    .build();

            kafkaTemplate.send(message);

            log.info("File sent to Kafka: documentId={}, fileName={}, contentType={}, size={} bytes",
                    documentId, fileName, contentType, data.length);

        } catch (Exception e) {
            log.error("Failed to send file to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send file to Kafka", e);
        }
    }
}