package com.smartchain.platform.global.util;

/**
 * 개인정보 보호법 제29조(안전조치의무) 준수를 위한 이름 마스킹 유틸리티.
 *
 * 마스킹 규칙: 첫 글자 + '*' × (길이-2) + 마지막 글자
 * - 1글자: 그대로 (김 → 김)
 * - 2글자: 첫 글자 + * (이수 → 이*)
 * - 3글자: 첫 글자 + * + 마지막 (홍길동 → 홍*동)
 * - 4글자+: 첫 글자 + ** ... + 마지막 (남궁선우 → 남**우)
 */
public final class NameMaskingUtil {

    private NameMaskingUtil() {
    }

    public static String mask(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }

        int length = name.length();

        if (length == 1) {
            return name;
        }

        if (length == 2) {
            return name.charAt(0) + "*";
        }

        return name.charAt(0)
                + "*".repeat(length - 2)
                + name.charAt(length - 1);
    }
}
