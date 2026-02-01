INSERT INTO role (name, code) VALUES ('게스트', 'GUEST') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('기안자', 'DRAFTER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('결재자', 'APPROVER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('심사자', 'REVIEWER') ON CONFLICT (code) DO NOTHING;

-- 기존 사용자 이메일 인증 활성화 (email_verified 컬럼 추가 이전 생성된 계정 대응)
UPDATE "User" SET email_verified = true WHERE email_verified = false;
