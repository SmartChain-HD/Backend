package com.smartchain.platform.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.dto.auth.login.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("특수문자(!)가 백슬래시 이스케이프된 비밀번호도 정상 파싱되어야 한다")
    void shouldParseBackslashEscapedSpecialCharacters() throws Exception {
        // \! 는 표준 JSON 이스케이프가 아니지만, 클라이언트가 보낼 수 있으므로 허용
        String json = "{\"email\":\"test@test.com\",\"password\":\"Test1234\\!\"}";

        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        assertEquals("test@test.com", request.getEmail());
        assertEquals("Test1234!", request.getPassword());
    }

    @Test
    @DisplayName("특수문자가 포함된 정상 JSON 비밀번호도 파싱되어야 한다")
    void shouldParseNormalSpecialCharacters() throws Exception {
        String json = "{\"email\":\"test@test.com\",\"password\":\"Test1234!\"}";

        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        assertEquals("test@test.com", request.getEmail());
        assertEquals("Test1234!", request.getPassword());
    }
}
