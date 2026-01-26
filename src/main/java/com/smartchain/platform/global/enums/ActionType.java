package com.smartchain.platform.global.enums;

/**
 * 활동 로그 액션 유형
 */
public enum ActionType {
    LOGIN("로그인"),
    LOGOUT("로그아웃"),
    UPLOAD("파일 업로드"),
    DOWNLOAD("파일 다운로드"),
    SUBMIT("제출"),
    APPROVE("승인"),
    REJECT("반려"),
    CREATE("생성"),
    UPDATE("수정"),
    DELETE("삭제");

    private final String displayName;

    ActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}