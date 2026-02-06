package com.smartchain.platform.domain.file.controller;

import com.smartchain.platform.domain.file.service.FileService;
import com.smartchain.platform.dto.common.file.*;
import com.smartchain.platform.global.response.BaseResponse;
import java.util.List;
import com.smartchain.platform.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "File", description = "파일 관리 API")
public class FileController {

    private final FileService fileService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "파일 업로드", description = "진단에 파일을 업로드합니다. 비동기로 파싱이 진행됩니다. DRAFTER 권한 필요.")
    @PostMapping(value = "/api/v1/diagnostics/{diagnosticId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<FileUploadResponse>> uploadFile(
            HttpServletRequest request,
            @Parameter(description = "진단 ID")
            @PathVariable Long diagnosticId,
            @Parameter(description = "업로드할 파일")
            @RequestParam("file") MultipartFile file) {
        Long userId = extractUserIdFromRequest(request);
        FileUploadResponse response = fileService.uploadFile(userId, diagnosticId, file);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(BaseResponse.success("파일 업로드가 시작되었습니다", response));
    }

    @Operation(summary = "파일 목록 조회", description = "진단에 업로드된 파일 목록을 조회합니다.")
    @GetMapping("/api/v1/diagnostics/{diagnosticId}/files")
    public ResponseEntity<BaseResponse<List<EvidenceFileDto>>> getFileList(
            HttpServletRequest request,
            @Parameter(description = "진단 ID")
            @PathVariable Long diagnosticId) {
        Long userId = extractUserIdFromRequest(request);
        List<EvidenceFileDto> response = fileService.getFileList(userId, diagnosticId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "파싱 결과 조회", description = "파일의 파싱 결과를 조회합니다. DRAFTER, APPROVER 권한 필요.")
    @GetMapping("/api/v1/diagnostics/{diagnosticId}/files/{fileId}/parsing-result")
    public ResponseEntity<BaseResponse<FileParsingResultResponse>> getParsingResult(
            HttpServletRequest request,
            @Parameter(description = "진단 ID")
            @PathVariable Long diagnosticId,
            @Parameter(description = "파일 ID")
            @PathVariable Long fileId) {
        Long userId = extractUserIdFromRequest(request);
        FileParsingResultResponse response = fileService.getParsingResult(userId, diagnosticId, fileId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "파일 다운로드 URL 발급", description = "파일 다운로드를 위한 Signed URL을 발급합니다. 30분간 유효합니다.")
    @GetMapping("/api/v1/files/{fileId}/download-url")
    public ResponseEntity<BaseResponse<FileDownloadUrlResponse>> getDownloadUrl(
            HttpServletRequest request,
            @Parameter(description = "파일 ID")
            @PathVariable Long fileId) {
        Long userId = extractUserIdFromRequest(request);
        FileDownloadUrlResponse response = fileService.getDownloadUrl(userId, fileId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "파일 직접 다운로드", description = "파일을 직접 다운로드합니다. local 환경에서 사용됩니다.")
    @GetMapping("/api/v1/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            HttpServletRequest request,
            @Parameter(description = "파일 ID")
            @PathVariable Long fileId) {
        Long userId = extractUserIdFromRequest(request);
        FileService.FileDownloadData data = fileService.downloadFile(userId, fileId);

        String contentDisposition = "attachment; filename=\"" + data.fileName() + "\"";
        MediaType mediaType = data.mimeType() != null
                ? MediaType.parseMediaType(data.mimeType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(mediaType)
                .contentLength(data.fileSize())
                .body(new InputStreamResource(data.inputStream()));
    }

    @Operation(summary = "파일 삭제", description = "파일을 삭제합니다. 제출된 진단의 파일은 삭제할 수 없습니다.")
    @DeleteMapping("/api/v1/files/{fileId}")
    public ResponseEntity<BaseResponse<FileDeleteResponse>> deleteFile(
            HttpServletRequest request,
            @Parameter(description = "파일 ID")
            @PathVariable Long fileId) {
        Long userId = extractUserIdFromRequest(request);
        FileDeleteResponse response = fileService.deleteFile(userId, fileId);
        return ResponseEntity.ok(BaseResponse.success(response.getMessage(), response));
    }

    @Operation(summary = "데이터 패키지 다운로드 URL 발급", description = "진단의 모든 파일을 포함한 패키지 다운로드 URL을 발급합니다. APPROVER, REVIEWER 권한 필요.")
    @GetMapping("/api/v1/files/diagnostics/{diagnosticId}/package-url")
    public ResponseEntity<BaseResponse<DataPackageUrlResponse>> getPackageUrl(
            HttpServletRequest request,
            @Parameter(description = "진단 ID")
            @PathVariable Long diagnosticId) {
        Long userId = extractUserIdFromRequest(request);
        DataPackageUrlResponse response = fileService.getPackageUrl(userId, diagnosticId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Authorization header is missing or invalid");
    }
}