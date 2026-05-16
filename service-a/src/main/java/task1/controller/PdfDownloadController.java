package task1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import task1.dto.DocumentMetadata;
import task1.service.MinioService;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
public class PdfDownloadController {

    private final MinioService minioService;

    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> downloadPdfWithSignature(@PathVariable String documentId) {
        try {
            log.info("Download request for documentId: {}", documentId);

            DocumentMetadata metadata = minioService.loadMetadata(documentId);

            String pdfPath = documentId + "/" + metadata.getPdfFileName();
            byte[] pdfBytes = minioService.getFile(pdfPath);

            String sigPath = documentId + "/" + metadata.getSignatureFileName();
            byte[] signatureBytes = minioService.getFile(sigPath);

            byte[] zipBytes = createZipArchive(
                    pdfBytes,
                    metadata.getPdfFileName(),
                    signatureBytes,
                    metadata.getSignatureFileName()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + documentId + ".zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);

        } catch (Exception e) {
            log.error("Failed to download document {}: {}", documentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    private byte[] createZipArchive(byte[] pdfBytes, String pdfFileName,
                                    byte[] signatureBytes, String signatureFileName) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry pdfEntry = new ZipEntry(pdfFileName);
            zos.putNextEntry(pdfEntry);
            zos.write(pdfBytes);
            zos.closeEntry();

            ZipEntry sigEntry = new ZipEntry(signatureFileName);
            zos.putNextEntry(sigEntry);
            zos.write(signatureBytes);
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        }
    }
}