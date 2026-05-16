package task1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import task1.kafka.PdfKafkaProducer;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfKafkaProducer pdfKafkaProducer;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile pdfFile,
            @RequestParam(value = "signature", required = false) MultipartFile signatureFile) {

        try {
            if (pdfFile == null || pdfFile.isEmpty()) {
                return ResponseEntity.badRequest().body("PDF file is empty");
            }

            String documentId = UUID.randomUUID().toString();
            byte[] pdfBytes = pdfFile.getBytes();

            pdfKafkaProducer.sendPdfFile(
                    pdfBytes,
                    pdfFile.getOriginalFilename(),
                    "application/pdf",
                    documentId
            );
            log.info("PDF sent to Kafka: documentId={}, fileName={}",
                    documentId, pdfFile.getOriginalFilename());

            if (signatureFile != null && !signatureFile.isEmpty()) {
                byte[] signatureBytes = signatureFile.getBytes();
                String signatureName = signatureFile.getOriginalFilename();
                String contentType = signatureFile.getContentType();

                if (contentType == null || contentType.isBlank()) {
                    contentType = "application/pkcs7-signature";
                }

                pdfKafkaProducer.sendPdfFile(
                        signatureBytes,
                        signatureName,
                        contentType,
                        documentId
                );
                log.info("Signature sent to Kafka: documentId={}, fileName={}",
                        documentId, signatureName);
            } else {
                log.warn("No signature file provided for documentId={}", documentId);
            }

            return ResponseEntity.ok("Document uploaded successfully. Document ID: " + documentId);

        } catch (Exception e) {
            log.error("Failed to upload PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload PDF: " + e.getMessage());
        }
    }
}