package com.smartchain.platform.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 도메인별 권한 검증 어노테이션.
 *
 * <p>Controller 메서드에 선언하면 AOP가 요청 파라미터/바디에서
 * domainCode를 추출하여 현재 사용자의 해당 도메인 역할을 검증합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @DomainAuthorization(roles = {"DRAFTER", "APPROVER"})
 * @PostMapping("/diagnostics")
 * public ResponseEntity<?> create(HttpServletRequest request, @RequestBody CreateRequest body) { ... }
 *
 * @DomainAuthorization(roles = {"REVIEWER"}, domainCodeParam = "domainCode")
 * @GetMapping("/reviews")
 * public ResponseEntity<?> list(HttpServletRequest request, @RequestParam String domainCode) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainAuthorization {

    /**
     * 허용되는 도메인 역할 코드 목록.
     * 사용자가 해당 도메인에서 이 역할들 중 하나라도 가지고 있어야 합니다.
     */
    String[] roles();

    /**
     * domainCode를 추출할 요청 파라미터/필드 이름.
     * 기본값은 "domainCode"이며, 요청 파라미터 → 요청 바디 순서로 탐색합니다.
     */
    String domainCodeParam() default "domainCode";
}
