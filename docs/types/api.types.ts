/**
 * SmartChain ESG Platform - API Type Definitions
 *
 * 이 파일은 백엔드 API와 연동하기 위한 TypeScript 타입 정의입니다.
 * 프론트엔드 프로젝트에 복사하여 사용하세요.
 *
 * @version 1.0
 * @lastUpdated 2026-01-20
 */

// ============================================
// 1. 공통 타입 (Common Types)
// ============================================

/** 기본 API 응답 */
export interface BaseResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

/** 에러 응답 */
export interface ErrorResponse {
  status: number;
  code: string;
  message: string;
}

/** 페이지 정보 */
export interface PageDto {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** 페이징된 데이터 */
export interface PagedData<T> {
  content: T[];
  page: PageDto;
}

// ============================================
// 2. Enum 타입 (Enums)
// ============================================

/** 역할 코드 */
export type RoleCode = 'GUEST' | 'DRAFTER' | 'APPROVER' | 'REVIEWER';

/** 도메인 코드 */
export type DomainCode = 'ESG' | 'SAFETY' | 'COMPLIANCE';

/** 권한 요청 상태 */
export type RequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

/** 진단 상태 */
export type DiagnosticStatus = 'WRITING' | 'SUBMITTED' | 'RETURNED' | 'APPROVED' | 'REVIEWING' | 'COMPLETED';

/** 결재 상태 */
export type ApprovalStatus = 'WAITING' | 'APPROVED' | 'REJECTED';

/** 심사 상태 */
export type ReviewStatus = 'REVIEWING' | 'APPROVED' | 'REVISION_REQUIRED';

/** 위험 등급 */
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

// ============================================
// 3. 공통 DTO (Common DTOs)
// ============================================

/** 간단한 사용자 정보 */
export interface UserSimpleDto {
  userId: number;
  name: string;
  email: string;
}

/** 역할 정보 */
export interface RoleSimpleDto {
  code: RoleCode;
  name: string;
}

/** 역할 상세 정보 */
export interface RoleInfoDto {
  code: RoleCode;
  name: string;
}

/** 간단한 회사 정보 */
export interface CompanySimpleDto {
  companyId: number;
  companyName: string;
}

/** 회사 상세 정보 */
export interface CompanyInfoDto {
  companyId: number;
  companyName: string;
}

/** 처리자 정보 */
export interface ProcessedByDto {
  userId: number;
  name: string;
}

// ============================================
// 4. Auth API 타입 (Authentication)
// ============================================

// --- 회원가입 ---
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  passwordConfirm: string;
  terms: TermsAgreementRequest;
}

export interface TermsAgreementRequest {
  privacyPolicyAgreed: boolean;
  serviceTermsAgreed: boolean;
  marketingAgreed?: boolean;
}

export interface RegisterResponse {
  userId: number;
  email: string;
  name: string;
  role: RoleCode;
  message: string;
  nextStep: string;
}

// --- 로그인 ---
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfoDto;
}

export interface UserInfoDto {
  userId: number;
  email: string;
  name: string;
  role?: RoleInfoDto;
  domainRoles?: UserDomainRole[];
  company?: CompanyInfoDto;
}

// --- 토큰 갱신 ---
export interface TokenRefreshRequest {
  refreshToken: string;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

// --- 로그아웃 ---
export interface LogoutRequest {
  refreshToken: string;
}

// --- 내 정보 조회 ---
export interface MyInfoResponse {
  userId: number;
  email: string;
  name: string;
  role?: RoleInfoDto;
  company?: CompanyInfoDto;
  lastLoginAt?: string;
  createdAt: string;
}

// --- 이메일 ---
export interface EmailCheckRequest {
  email: string;
}

export interface EmailCheckResponse {
  email: string;
  available: boolean;
  message: string;
}

export interface SendVerificationRequest {
  email: string;
}

export interface SendVerificationResponse {
  email: string;
  message: string;
  expiresInSeconds: number;
}

export interface EmailVerificationRequest {
  email: string;
  verificationCode: string;
}

export interface EmailVerificationResponse {
  verified: boolean;
  message: string;
}

// ============================================
// 5. Role Request API 타입 (권한 요청)
// ============================================

// --- 권한 요청 페이지 정보 ---
export interface RoleRequestPageResponse {
  currentRole: RoleSimpleDto;
  currentDomainRoles: UserDomainRole[];
  availableRoles: RoleOptionDto[];
  availableDomains: DomainOptionDto[];
  availableCompanies: CompanyOptionDto[];
  pendingRequest?: RoleRequestStatusDto;
}

export interface RoleOptionDto {
  roleCode: RoleCode;
  roleName: string;
  description: string;
  iconUrl: string;
  selectable: boolean;
}

export interface CompanyOptionDto {
  companyId: number;
  companyName: string;
  companyType: string;
}

/** 도메인 옵션 DTO */
export interface DomainOptionDto {
  code: DomainCode;
  name: string;
}

/** 사용자 도메인별 역할 */
export interface UserDomainRole {
  domainCode: DomainCode;
  domainName: string;
  roleCode: RoleCode;
  roleName: string;
}

// --- 권한 요청 생성 ---
export interface RoleRequestCreateDto {
  requestedRole: RoleCode;
  domainCode: DomainCode;
  companyId: number;
  reason?: string;
}

export interface RoleRequestResponse {
  accessRequestId: number;
  status: RequestStatus;
  requestedRole: string;
  companyId: number;
  createdAt: string;
  message: string;
}

// --- 내 권한 요청 상태 ---
export interface RoleRequestStatusDto {
  accessRequestId: number;
  requestedRole: RoleSimpleDto;
  domain?: DomainOptionDto;
  company: CompanySimpleDto;
  status: RequestStatus;
  statusLabel: string;
  reason?: string;
  requestedAt: string;
  processedAt?: string;
  processedBy?: ProcessedByDto;
  rejectReason?: string;
}

// ============================================
// 6. Role Approval API 타입 (권한 처리 - 관리자)
// ============================================

// --- 권한 요청 목록 조회 ---
export interface RoleApprovalListRequest {
  status?: RequestStatus;
  companyId?: number;
  page?: number;
  size?: number;
}

export interface RoleApprovalListResponse {
  content: RoleApprovalItemDto[];
  page: PageDto;
}

export interface RoleApprovalItemDto {
  accessRequestId: number;
  user: UserSimpleDto;
  company: CompanySimpleDto;
  requestedRole: RoleSimpleDto;
  domain?: DomainOptionDto;
  status: RequestStatus;
  reason?: string;
  requestedAt: string;
  requestedAtLabel: string;
}

// --- 권한 요청 상세 ---
export interface RoleApprovalDetailResponse {
  accessRequestId: number;
  user: UserSimpleDto;
  requestedRole: RoleSimpleDto;
  domain?: DomainOptionDto;
  company: CompanySimpleDto;
  status: RequestStatus;
  reason?: string;
  requestedAt: string;
}

// --- 권한 승인/반려 ---
export interface RoleDecisionRequest {
  decision: 'APPROVED' | 'REJECTED';
  rejectReason?: string;
}

export interface RoleDecisionResponse {
  accessRequestId: number;
  status: RequestStatus;
  processedAt: string;
  processedBy: ProcessedByDto;
  message: string;
}

// ============================================
// 7. 에러 코드 상수
// ============================================

export const ERROR_CODES = {
  // 400 Bad Request
  INVALID_INPUT: 'U001',
  DUPLICATE_EMAIL: 'U002',
  PASSWORD_MISMATCH: 'U004',
  INVALID_PASSWORD_FORMAT: 'U005',
  TERMS_NOT_AGREED: 'U006',
  EMAIL_NOT_VERIFIED: 'U007',
  VERIFICATION_CODE_EXPIRED: 'U008',
  INVALID_VERIFICATION_CODE: 'U009',
  INVALID_ROLE_REQUEST: 'R004',
  INVALID_DECISION: 'R005',

  // 401 Unauthorized
  INVALID_TOKEN: 'A001',
  EXPIRED_TOKEN: 'A002',
  INVALID_CREDENTIALS: 'A003',

  // 403 Forbidden
  ACCESS_DENIED: 'A004',
  PERMISSION_DENIED_RESOURCE: 'PERM_001',
  PERMISSION_DENIED_ACTION: 'PERM_002',

  // 404 Not Found
  USER_NOT_FOUND: 'U003',
  ROLE_NOT_FOUND: 'R001',
  ROLE_REQUEST_NOT_FOUND: 'R002',
  COMPANY_NOT_FOUND: 'C001',
  DIAGNOSTIC_NOT_FOUND: 'D001',
  EVIDENCE_NOT_FOUND: 'E001',

  // 409 Conflict
  DUPLICATE_ROLE_REQUEST: 'R003',
  ALREADY_PROCESSED_REQUEST: 'PERM_003',

  // 429 Too Many Requests
  VERIFICATION_RATE_LIMIT: 'U010',

  // 500 Internal Server Error
  INTERNAL_ERROR: 'S001',
  FILE_UPLOAD_ERROR: 'S002',
  AI_SERVICE_ERROR: 'S003',
} as const;

export type ErrorCode = typeof ERROR_CODES[keyof typeof ERROR_CODES];

// ============================================
// 8. 상태 레이블 매핑
// ============================================

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = {
  PENDING: '승인 대기중',
  APPROVED: '승인 완료',
  REJECTED: '승인 반려',
};

export const ROLE_LABELS: Record<RoleCode, string> = {
  GUEST: '게스트',
  DRAFTER: '기안자',
  APPROVER: '결재자',
  REVIEWER: '수신자',
};

export const ROLE_DESCRIPTIONS: Record<Exclude<RoleCode, 'GUEST'>, string> = {
  DRAFTER: 'ESG 자가 기안 데이터 업로드',
  APPROVER: '회사 정보 관리, ESG 파일 관리, 협력사 권한 관리',
  REVIEWER: '심사, 보고서 발행, 권한 관리',
};

export const DOMAIN_LABELS: Record<DomainCode, string> = {
  ESG: 'ESG 실사',
  SAFETY: '안전보건',
  COMPLIANCE: '컴플라이언스',
};

export const DOMAIN_DESCRIPTIONS: Record<DomainCode, string> = {
  ESG: 'ESG 증빙 자동 파싱 및 AI 리포트 생성',
  SAFETY: 'AI 기반 현장 안전점검(TBM) 자동 검증',
  COMPLIANCE: 'LLM 기반 하도급 계약서 자동 검토',
};
