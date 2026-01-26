package com.smartchain.platform;

import com.smartchain.platform.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Health", description = "서버 상태 확인")
@RestController
public class HealthController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 작동 중인지 확인합니다")
    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> health() {
        return BaseResponse.success(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now()
        ));
    }
}
