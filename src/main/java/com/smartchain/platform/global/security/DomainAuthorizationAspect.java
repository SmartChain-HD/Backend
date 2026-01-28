package com.smartchain.platform.global.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.global.error.CustomException;
import com.smartchain.platform.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DomainAuthorizationAspect {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Before("@annotation(domainAuthorization)")
    public void checkDomainAuthorization(JoinPoint joinPoint, DomainAuthorization domainAuthorization) {
        String[] requiredRoles = domainAuthorization.roles();
        String domainCodeParam = domainAuthorization.domainCodeParam();

        // 1. HttpServletRequest에서 userId 추출
        HttpServletRequest request = getCurrentRequest();
        Long userId = extractUserId(request);

        // 2. domainCode 추출 (파라미터 → 바디)
        String domainCode = extractDomainCode(request, joinPoint.getArgs(), domainCodeParam);
        if (!StringUtils.hasText(domainCode)) {
            log.warn("domainCode not found in request for param '{}'", domainCodeParam);
            throw new CustomException(ErrorCode.INVALID_INPUT, "domainCode가 요청에 포함되어 있지 않습니다");
        }

        // 3. 사용자 조회 및 도메인 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        boolean hasPermission = user.hasAnyRoleInDomain(domainCode, requiredRoles);
        if (!hasPermission) {
            log.warn("Domain authorization failed: userId={}, domainCode={}, requiredRoles={}",
                    userId, domainCode, Arrays.toString(requiredRoles));
            throw new CustomException(ErrorCode.PERMISSION_DENIED_ACTION);
        }

        log.debug("Domain authorization passed: userId={}, domainCode={}, roles={}",
                userId, domainCode, Arrays.toString(requiredRoles));
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "요청 컨텍스트를 찾을 수 없습니다");
        }
        return attrs.getRequest();
    }

    private Long extractUserId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    private String extractDomainCode(HttpServletRequest request, Object[] args, String paramName) {
        // 1. 요청 파라미터에서 추출
        String domainCode = request.getParameter(paramName);
        if (StringUtils.hasText(domainCode)) {
            return domainCode;
        }

        // 2. 메서드 인자(RequestBody)에서 추출
        for (Object arg : args) {
            if (arg == null || arg instanceof HttpServletRequest) {
                continue;
            }
            domainCode = extractFromObject(arg, paramName);
            if (StringUtils.hasText(domainCode)) {
                return domainCode;
            }
        }

        return null;
    }

    private String extractFromObject(Object obj, String fieldName) {
        try {
            JsonNode node = objectMapper.valueToTree(obj);
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode.asText();
            }
        } catch (Exception e) {
            log.debug("Failed to extract '{}' from object of type {}: {}", fieldName, obj.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }
}
