package com.smartchain.platform.domain.file.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;

public interface FileStorageService {

    String upload(MultipartFile file, String path);

    InputStream download(String path);

    String getPresignedUrl(String path, Duration expiry);

    void delete(String path);
}
