package com.smartchain.platform.domain.file.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
@Service
@Profile("azure")
public class AzureBlobStorageService implements FileStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
            log.info("Azure Blob container created: {}", containerName);
        }
    }

    @Override
    public String upload(MultipartFile file, String path) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(path);
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);
            log.info("File uploaded to Azure Blob: path={}, size={}", path, file.getSize());
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            log.error("Failed to upload file to Azure Blob: path={}", path, e);
            throw new RuntimeException("파일 업로드에 실패했습니다", e);
        }
    }

    @Override
    public InputStream download(String path) {
        BlobClient blobClient = containerClient.getBlobClient(path);
        return blobClient.openInputStream();
    }

    @Override
    public String getPresignedUrl(String path, Duration expiry) {
        String blobPath = normalizeBlobPath(path);
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plus(expiry), permission);
        String sasToken = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

    @Override
    public void delete(String path) {
        BlobClient blobClient = containerClient.getBlobClient(path);
        if (blobClient.exists()) {
            blobClient.delete();
            log.info("File deleted from Azure Blob: path={}", path);
        }
    }

    private String normalizeBlobPath(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return pathOrUrl;
        }

        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            URI uri = URI.create(pathOrUrl);
            String rawPath = uri.getPath();
            String decodedPath = rawPath == null ? null : URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
            String containerPrefix = "/" + containerName + "/";
            if (decodedPath != null && decodedPath.startsWith(containerPrefix)) {
                return decodedPath.substring(containerPrefix.length());
            }
            return decodedPath != null && decodedPath.startsWith("/") ? decodedPath.substring(1) : decodedPath;
        }

        return pathOrUrl;
    }
}
