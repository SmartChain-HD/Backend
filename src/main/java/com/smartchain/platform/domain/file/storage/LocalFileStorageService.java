package com.smartchain.platform.domain.file.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Slf4j
@Service
@Profile("!azure")
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.storage.local.base-path:./uploads}")
    private String basePath;

    @Override
    public String upload(MultipartFile file, String path) {
        try {
            Path targetPath = Paths.get(basePath, path);
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded to local storage: path={}, size={}", path, file.getSize());
            return path;
        } catch (IOException e) {
            log.error("Failed to upload file to local storage: path={}", path, e);
            throw new RuntimeException("파일 업로드에 실패했습니다", e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            Path filePath = resolvePath(path);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("Failed to download file from local storage: path={}", path, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다", e);
        }
    }

    @Override
    public String getPresignedUrl(String path, Duration expiry) {
        // 로컬 환경에서는 API 경로 반환
        return "/api/v1/files/local/" + path;
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = resolvePath(path);
            Files.deleteIfExists(filePath);
            log.info("File deleted from local storage: path={}", path);
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: path={}", path, e);
        }
    }

    private Path resolvePath(String path) {
        Path resolved = Paths.get(path);
        if (resolved.isAbsolute()) {
            return resolved;
        }
        return Paths.get(basePath, path);
    }
}
