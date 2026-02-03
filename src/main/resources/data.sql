INSERT INTO role (name, code) VALUES ('게스트', 'GUEST') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('기안자', 'DRAFTER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('결재자', 'APPROVER') ON CONFLICT (code) DO NOTHING;
INSERT INTO role (name, code) VALUES ('심사자', 'REVIEWER') ON CONFLICT (code) DO NOTHING;

-- 기존 사용자 이메일 인증 활성화 (email_verified 컬럼 추가 이전 생성된 계정 대응)
UPDATE "user" SET email_verified = true WHERE email_verified = false;

-- 진단 코드 시퀀스 생성 (동시 요청 시 중복 코드 방지)
CREATE SEQUENCE IF NOT EXISTS diagnostic_code_seq START WITH 1 INCREMENT BY 1;
SELECT setval('diagnostic_code_seq', COALESCE((SELECT MAX(diagnostic_id) FROM diagnostic), 0) + 1, false);
