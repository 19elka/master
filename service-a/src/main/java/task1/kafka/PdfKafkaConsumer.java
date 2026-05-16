package task1.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import task1.dto.DocumentMetadata;
import task1.service.MinioService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfKafkaConsumer {

    private final MinioService minioService;
    private final Map<String, FilePart> pendingFiles = new ConcurrentHashMap<>();
    private final Map<String, DocumentMetadata> pendingMetadata = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @KafkaListener(topics = "pdf-documents", groupId = "pdf-consumer-group", containerFactory = "pdfKafkaListenerContainerFactory")
    public void consumeFile(ConsumerRecord<String, byte[]> record) {
        try {
            String documentId = new String(record.headers().lastHeader("document-id").value());
            String fileName = new String(record.headers().lastHeader("file-name").value());
            String contentType = new String(record.headers().lastHeader("content-type").value());
            byte[] data = record.value();

            log.info("Received file: documentId={}, fileName={}, contentType={}, size={} bytes",
                    documentId, fileName, contentType, data.length);

            String objectName = documentId + "/" + fileName;
            minioService.saveFile(objectName, data, contentType);

            DocumentMetadata metadata = pendingMetadata.computeIfAbsent(documentId, id -> new DocumentMetadata());

            if (fileName.toLowerCase().endsWith(".pdf")) {
                metadata.setPdfFileName(fileName);
                log.info("PDF received for documentId={}, fileName={}", documentId, fileName);
            } else if (fileName.toLowerCase().endsWith(".sig") ||
                    fileName.toLowerCase().endsWith(".p7s") ||
                    fileName.toLowerCase().endsWith(".sgn")) {
                metadata.setSignatureFileName(fileName);
                log.info("Signature received for documentId={}, fileName={}", documentId, fileName);
            }

            pendingMetadata.put(documentId, metadata);

            FilePart part = pendingFiles.computeIfAbsent(documentId, id -> {
                scheduleTimeout(documentId);
                return new FilePart();
            });

            if (fileName.toLowerCase().endsWith(".pdf")) {
                part.pdfReceived = true;
            } else {
                part.signatureReceived = true;
            }

            if (part.pdfReceived && part.signatureReceived) {
                minioService.saveMetadata(documentId, metadata);
                log.info("All files received for documentId={}, metadata saved", documentId);
                pendingFiles.remove(documentId);
                pendingMetadata.remove(documentId);
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
        }
    }

    private void scheduleTimeout(String documentId) {
        scheduler.schedule(() -> {
            FilePart part = pendingFiles.get(documentId);
            if (part != null) {
                DocumentMetadata metadata = pendingMetadata.get(documentId);
                if (metadata != null) {
                    minioService.saveMetadata(documentId, metadata);
                    log.warn("Timeout for documentId={}, saved partial metadata", documentId);
                }
                pendingFiles.remove(documentId);
                pendingMetadata.remove(documentId);
            }
        }, 30, TimeUnit.SECONDS);
    }

    private static class FilePart {
        boolean pdfReceived = false;
        boolean signatureReceived = false;
    }
}