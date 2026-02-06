-- SmartChain 테스트용 시드 데이터
-- data.sql에 Role만 있으므로 나머지 필수 데이터 추가
--
-- 실행 방법 (Docker PostgreSQL):
--   docker exec -i smartchain-db psql -U esg -d smartchain < http/00_seed_test_data.sql

-- ========== 1. Domain ==========
INSERT INTO domain (code, name, description, is_active, created_at, updated_at)
VALUES
    ('ESG', 'ESG 실사', 'ESG 증빙 자동 파싱 및 AI 리포트 생성', true, NOW(), NOW()),
    ('SAFETY', '안전보건', 'TBM 영상 AI 분석 및 안전점검 검증', true, NOW(), NOW()),
    ('COMPLIANCE', '컴플라이언스', '하도급 계약서 LLM 자동 검토', true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- ========== 2. Industry ==========
-- industry 테이블에 unique constraint가 없으므로 중복 체크 후 삽입
INSERT INTO industry (name, code)
SELECT '제조업', 'MANUFACTURING'
WHERE NOT EXISTS (SELECT 1 FROM industry WHERE code = 'MANUFACTURING');

INSERT INTO industry (name, code)
SELECT '건설업', 'CONSTRUCTION'
WHERE NOT EXISTS (SELECT 1 FROM industry WHERE code = 'CONSTRUCTION');

INSERT INTO industry (name, code)
SELECT 'IT/소프트웨어', 'IT'
WHERE NOT EXISTS (SELECT 1 FROM industry WHERE code = 'IT');

-- ========== 3. Company ==========
-- CompanyType: TIER1, TIER2만 유효

-- 원청 회사 (REVIEWER 소속)
INSERT INTO company (name, scale, business_number, ceo_name, address, contact_email, contact_phone,
                     company_type, status, industry_id, created_at, updated_at)
SELECT '(주)스마트체인원청', '대기업', '123-45-67890', '홍길동', '서울시 강남구 테헤란로 123',
       'contact@smartchain-owner.com', '02-1234-5678', 'TIER1', 'ACTIVE',
       i.industry_id, NOW(), NOW()
FROM industry i WHERE i.code = 'MANUFACTURING'
AND NOT EXISTS (SELECT 1 FROM company WHERE business_number = '123-45-67890');

-- 협력사 (DRAFTER, APPROVER 소속)
INSERT INTO company (name, scale, business_number, ceo_name, address, contact_email, contact_phone,
                     company_type, status, industry_id, created_at, updated_at)
SELECT '(주)협력사A', '중소기업', '234-56-78901', '김철수', '경기도 성남시 분당구 판교로 456',
       'contact@partner-a.com', '031-2345-6789', 'TIER1', 'ACTIVE',
       i.industry_id, NOW(), NOW()
FROM industry i WHERE i.code = 'MANUFACTURING'
AND NOT EXISTS (SELECT 1 FROM company WHERE business_number = '234-56-78901');

-- ========== 3-1. 외부 리스크 분석 대상 협력사 ==========
-- DataInitializer.java로 이동됨 (섹션 2-1)

-- ========== 4. Campaign ==========
-- ESG 캠페인
INSERT INTO campaign (campaign_code, title, content, domain_id, owner_company_id,
                      period_start_date, period_end_date, deadline, created_at, updated_at)
SELECT 'ESG-2026-Q1', '2026년 1분기 ESG 진단', 'ESG 실사 진단 캠페인입니다.',
       d.domain_id, c.company_id,
       '2026-01-01', '2026-03-31', '2026-02-28', NOW(), NOW()
FROM domain d, company c
WHERE d.code = 'ESG' AND c.business_number = '123-45-67890'
ON CONFLICT (campaign_code) DO NOTHING;

-- SAFETY 캠페인
INSERT INTO campaign (campaign_code, title, content, domain_id, owner_company_id,
                      period_start_date, period_end_date, deadline, created_at, updated_at)
SELECT 'SAFETY-2026-Q1', '2026년 1분기 안전보건 점검', '안전보건 점검 캠페인입니다.',
       d.domain_id, c.company_id,
       '2026-01-01', '2026-03-31', '2026-02-28', NOW(), NOW()
FROM domain d, company c
WHERE d.code = 'SAFETY' AND c.business_number = '123-45-67890'
ON CONFLICT (campaign_code) DO NOTHING;

-- ========== 데이터 확인 쿼리 ==========
-- SELECT * FROM role;
-- SELECT * FROM domain;
-- SELECT * FROM industry;
-- SELECT * FROM company;
-- SELECT * FROM campaign;

-- 외부 리스크 대상 회사는 DataInitializer에서 생성됨
