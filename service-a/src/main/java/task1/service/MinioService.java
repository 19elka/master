package task1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import task1.config.MinioConfig;
import task1.dto.DocumentMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig.MinioProperties minioProperties;
    private final ObjectMapper objectMapper;

    public void createBucketIfNotExists() {
        try {
            String bucketName = minioProperties.bucket();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Bucket '{}' created", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to create bucket: {}", e.getMessage());
        }
    }

    public void saveFile(String objectName, byte[] data, String contentType) {
        try {
            createBucketIfNotExists();
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.bucket())
                        .object(objectName)
                        .stream(inputStream, data.length, -1)
                        .contentType(contentType)
                        .build());
                log.info("File saved: {}", objectName);
            }
        } catch (Exception e) {
            log.error("Failed to save file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Failed to save file to Minio", e);
        }
    }

    public byte[] getFile(String objectName) {
        try {
            try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectName)
                    .build())) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("Failed to get file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Failed to get file from Minio", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectName)
                    .build());
            log.info("File deleted: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Failed to delete file from Minio", e);
        }
    }

    public List<String> listObjects(String prefix) {
        try {
            Iterable<io.minio.Result<io.minio.messages.Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.bucket())
                            .prefix(prefix)
                            .build()
            );

            return StreamSupport.stream(results.spliterator(), false)
                    .map(itemResult -> {
                        try {
                            return itemResult.get().objectName();
                        } catch (Exception e) {
                            log.error("Failed to get object name: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list objects: {}", e.getMessage());
            throw new RuntimeException("Failed to list Minio objects", e);
        }
    }

    public void saveMetadata(String documentId, DocumentMetadata metadata) {
        try {
            byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);
            String objectName = documentId + "/metadata.json";
            saveFile(objectName, metadataBytes, "application/json");
            log.info("Metadata saved for documentId: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to save metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save metadata", e);
        }
    }

    public DocumentMetadata loadMetadata(String documentId) {
        try {
            String objectName = documentId + "/metadata.json";
            byte[] metadataBytes = getFile(objectName);
            return objectMapper.readValue(metadataBytes, DocumentMetadata.class);
        } catch (Exception e) {
            log.error("Failed to load metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load metadata", e);
        }
    }
}