package com.smartchain.platform.domain.file.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileStorageServiceTest {

    private LocalFileStorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(storageService, "basePath", tempDir.toString());
    }

    @Test
    @DisplayName("파일 업로드 후 다운로드할 수 있다")
    void uploadAndDownload() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());
        String path = "diagnostics/1/abc_test.pdf";

        // when
        String result = storageService.upload(file, path);

        // then
        assertThat(result).isNotNull();
        assertThat(Files.exists(tempDir.resolve(path))).isTrue();

        // download
        InputStream downloaded = storageService.download(path);
        assertThat(new String(downloaded.readAllBytes())).isEqualTo("test content");
        downloaded.close();
    }

    @Test
    @DisplayName("presignedUrl은 로컬 경로를 반환한다")
    void getPresignedUrl_returnsLocalPath() {
        String url = storageService.getPresignedUrl("diagnostics/1/test.pdf", Duration.ofMinutes(30));
        assertThat(url).contains("diagnostics/1/test.pdf");
    }

    @Test
    @DisplayName("파일 삭제 시 파일이 제거된다")
    void delete_removesFile() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());
        String path = "diagnostics/1/del_test.pdf";
        storageService.upload(file, path);
        assertThat(Files.exists(tempDir.resolve(path))).isTrue();

        // when
        storageService.delete(path);

        // then
        assertThat(Files.exists(tempDir.resolve(path))).isFalse();
    }
}
