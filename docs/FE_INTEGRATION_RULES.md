# Frontend Integration Rules (프론트엔드 연동 규칙)

> **버전**: 2.1
> **최종 수정**: 2026-01-28
> **목적**: 프론트엔드 개발자를 위한 API 연동 가이드

---

## 0. 서비스 도메인 개요

플랫폼은 3개의 서비스 도메인으로 구성됩니다:

| 도메인 코드 | 이름 | 설명 |
|------------|------|------|
| `ESG` | ESG 실사 | ESG 증빙 자동 파싱 및 AI 리포트 생성 |
| `SAFETY` | 안전보건 | AI 기반 현장 안전점검(TBM) 자동 검증 |
| `COMPLIANCE` | 컴플라이언스 | LLM 기반 하도급 계약서 자동 검토 |

### 역할별 도메인 권한

사용자는 각 도메인별로 다른 역할을 가질 수 있습니다:

```typescript
// 도메인별 역할 예시
interface UserDomainRole {
  domainCode: 'ESG' | 'SAFETY' | 'COMPLIANCE';
  roleCode: 'DRAFTER' | 'APPROVER' | 'REVIEWER';
}

// 사용자 정보 응답
interface UserInfoDto {
  userId: number;
  email: string;
  name: string;
  role?: RoleInfoDto;        // 레거시 전역 역할
  domainRoles: UserDomainRole[]; // 도메인별 역할
  company?: CompanyInfoDto;
}
```

---

## 1. API 클라이언트 설정

### 1.1 Axios 인스턴스 설정

```typescript
// api/client.ts
import axios, { AxiosInstance, AxiosError } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor - 토큰 자동 첨부
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor - 에러 핸들링
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ErrorResponse>) => {
    if (error.response?.status === 401) {
      const errorCode = error.response.data?.code;

      if (errorCode === 'A002') {
        // 토큰 만료 - 갱신 시도
        const refreshed = await refreshToken();
        if (refreshed) {
          return apiClient.request(error.config!);
        }
      }
      // 로그인 페이지로 이동
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 1.2 타입 정의

```typescript
// types/api.ts

// 기본 응답 타입
interface BaseResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// 에러 응답 타입
interface ErrorResponse {
  status: number;
  code: string;
  message: string;
}

// 페이지네이션 타입
interface PageDto {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface PagedData<T> {
  content: T[];
  page: PageDto;
}
```

---

## 2. 인증 플로우 (Authentication Flow)

### 2.1 로그인

```typescript
// api/auth.ts
interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfoDto;
}

export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<BaseResponse<LoginResponse>>('/auth/login', data);

  // 토큰 저장
  localStorage.setItem('accessToken', response.data.data.accessToken);
  localStorage.setItem('refreshToken', response.data.data.refreshToken);

  return response.data.data;
};
```

### 2.2 토큰 갱신

```typescript
export const refreshToken = async (): Promise<boolean> => {
  try {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;

    const response = await axios.post<BaseResponse<TokenRefreshResponse>>(
      `${BASE_URL}/auth/token/refresh`,
      { refreshToken }
    );

    localStorage.setItem('accessToken', response.data.data.accessToken);
    localStorage.setItem('refreshToken', response.data.data.refreshToken);
    return true;
  } catch {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    return false;
  }
};
```

### 2.3 로그아웃

```typescript
export const logout = async (): Promise<void> => {
  try {
    const refreshToken = localStorage.getItem('refreshToken');
    await apiClient.post('/auth/logout', { refreshToken });
  } finally {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login';
  }
};
```

---

## 3. 에러 핸들링 패턴

### 3.1 에러 핸들러 유틸리티

```typescript
// utils/errorHandler.ts
import { AxiosError } from 'axios';
import { toast } from 'react-toastify'; // 또는 사용하는 토스트 라이브러리

interface ErrorResponse {
  status: number;
  code: string;
  message: string;
}

type ErrorAction = 'toast' | 'redirect' | 'retry' | 'silent';

interface ErrorConfig {
  action: ErrorAction;
  redirectTo?: string;
  customMessage?: string;
}

// 에러 코드별 처리 설정
const ERROR_HANDLERS: Record<string, ErrorConfig> = {
  // 인증 에러
  'A001': { action: 'redirect', redirectTo: '/login' },
  'A002': { action: 'silent' }, // interceptor에서 처리
  'A003': { action: 'toast', customMessage: '이메일 또는 비밀번호를 확인해주세요' },

  // 권한 에러
  'A004': { action: 'toast' },
  'PERM_001': { action: 'toast' },
  'PERM_002': { action: 'toast' },
  'PERM_003': { action: 'toast', customMessage: '이미 처리된 요청입니다. 새로고침 해주세요.' },

  // 중복 에러
  'U002': { action: 'toast' },
  'R003': { action: 'toast' },

  // 404 에러
  'U003': { action: 'redirect', redirectTo: '/not-found' },
  'R002': { action: 'toast' },

  // Rate Limit
  'U010': { action: 'toast' },

  // 서버 에러
  'S001': { action: 'toast', customMessage: '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.' },
};

export const handleApiError = (error: AxiosError<ErrorResponse>): void => {
  const errorData = error.response?.data;
  const errorCode = errorData?.code || 'UNKNOWN';
  const errorMessage = errorData?.message || '알 수 없는 오류가 발생했습니다';

  const handler = ERROR_HANDLERS[errorCode] || { action: 'toast' };

  switch (handler.action) {
    case 'toast':
      toast.error(handler.customMessage || errorMessage);
      break;
    case 'redirect':
      window.location.href = handler.redirectTo || '/';
      break;
    case 'retry':
      // 재시도 로직
      break;
    case 'silent':
      // 로깅만
      console.error(`[${errorCode}] ${errorMessage}`);
      break;
  }
};
```

### 3.2 컴포넌트에서 사용

```typescript
// React Query 예시
import { useMutation } from '@tanstack/react-query';

const useCreateRoleRequest = () => {
  return useMutation({
    mutationFn: createRoleRequest,
    onSuccess: (data) => {
      toast.success(data.message);
      // 성공 처리
    },
    onError: (error: AxiosError<ErrorResponse>) => {
      handleApiError(error);
    },
  });
};
```

---

## 4. API 함수 작성 패턴

### 4.1 기본 패턴

```typescript
// api/roleRequest.ts
import { apiClient } from './client';
import { BaseResponse, PagedData } from '../types/api';

// 타입 정의
interface RoleRequestCreateDto {
  requestedRole: string;
  companyId: number;
  reason?: string;
}

interface RoleRequestResponse {
  accessRequestId: number;
  status: string;
  requestedRole: string;
  companyId: number;
  createdAt: string;
  message: string;
}

// API 함수
export const createRoleRequest = async (
  data: RoleRequestCreateDto
): Promise<RoleRequestResponse> => {
  const response = await apiClient.post<BaseResponse<RoleRequestResponse>>(
    '/roles/requests',
    data
  );
  return response.data.data;
};

export const getMyRoleRequestStatus = async (): Promise<RoleRequestStatusDto> => {
  const response = await apiClient.get<BaseResponse<RoleRequestStatusDto>>(
    '/roles/requests/my'
  );
  return response.data.data;
};
```

### 4.2 페이지네이션 패턴

```typescript
interface GetRoleRequestListParams {
  status?: string;
  companyId?: number;
  page?: number;
  size?: number;
}

export const getRoleRequestList = async (
  params: GetRoleRequestListParams = {}
): Promise<PagedData<RoleApprovalItemDto>> => {
  const response = await apiClient.get<BaseResponse<PagedData<RoleApprovalItemDto>>>(
    '/roles/requests',
    { params: { page: 0, size: 10, ...params } }
  );
  return response.data.data;
};
```

---

## 5. 상태 관리 연동 (State Management)

### 5.1 React Query 설정

```typescript
// lib/queryClient.ts
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5분
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});
```

### 5.2 Query Key 컨벤션

```typescript
// constants/queryKeys.ts
export const QUERY_KEYS = {
  // Auth
  AUTH: {
    MY_INFO: ['auth', 'myInfo'] as const,
  },

  // Role
  ROLE: {
    REQUEST_PAGE: ['role', 'requestPage'] as const,
    MY_STATUS: ['role', 'myStatus'] as const,
    LIST: (params?: object) => ['role', 'list', params] as const,
    DETAIL: (id: number) => ['role', 'detail', id] as const,
  },

  // Company
  COMPANY: {
    LIST: ['company', 'list'] as const,
  },
};
```

### 5.3 Custom Hook 패턴

```typescript
// hooks/useRoleRequest.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { QUERY_KEYS } from '../constants/queryKeys';
import * as roleApi from '../api/roleRequest';

export const useRoleRequestPage = () => {
  return useQuery({
    queryKey: QUERY_KEYS.ROLE.REQUEST_PAGE,
    queryFn: roleApi.getRoleRequestPageInfo,
  });
};

export const useRoleRequestList = (params?: GetRoleRequestListParams) => {
  return useQuery({
    queryKey: QUERY_KEYS.ROLE.LIST(params),
    queryFn: () => roleApi.getRoleRequestList(params),
  });
};

export const useCreateRoleRequest = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: roleApi.createRoleRequest,
    onSuccess: () => {
      // 관련 쿼리 무효화
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ROLE.REQUEST_PAGE });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ROLE.MY_STATUS });
    },
  });
};

export const useProcessRoleRequest = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: RoleDecisionRequest }) =>
      roleApi.processRoleRequest(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['role'] });
    },
  });
};
```

---

## 6. 폼 검증 규칙 (Form Validation)

### 6.1 회원가입 검증

```typescript
// validation/auth.ts
import { z } from 'zod';

export const registerSchema = z.object({
  name: z.string()
    .min(1, '이름을 입력해주세요')
    .max(50, '이름은 50자 이내로 입력해주세요'),

  email: z.string()
    .min(1, '이메일을 입력해주세요')
    .email('올바른 이메일 형식이 아닙니다'),

  password: z.string()
    .min(8, '비밀번호는 8자 이상이어야 합니다')
    .regex(
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/,
      '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다'
    ),

  passwordConfirm: z.string(),

  terms: z.object({
    privacyPolicyAgreed: z.literal(true, {
      errorMap: () => ({ message: '개인정보처리방침에 동의해주세요' }),
    }),
    serviceTermsAgreed: z.literal(true, {
      errorMap: () => ({ message: '서비스 이용약관에 동의해주세요' }),
    }),
    marketingAgreed: z.boolean().optional(),
  }),
}).refine((data) => data.password === data.passwordConfirm, {
  message: '비밀번호가 일치하지 않습니다',
  path: ['passwordConfirm'],
});
```

### 6.2 권한 요청 검증

```typescript
export const roleRequestSchema = z.object({
  requestedRole: z.enum(['DRAFTER', 'APPROVER', 'REVIEWER'], {
    errorMap: () => ({ message: '역할을 선택해주세요' }),
  }),
  domainCode: z.enum(['ESG', 'SAFETY', 'COMPLIANCE'], {
    errorMap: () => ({ message: '도메인을 선택해주세요' }),
  }),
  companyId: z.number({
    errorMap: () => ({ message: '회사를 선택해주세요' }),
  }).positive(),
  reason: z.string().max(500, '사유는 500자 이내로 입력해주세요').optional(),
});
```

### 6.3 진단 생성 검증 (도메인 포함)

```typescript
export const diagnosticCreateSchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요').max(200),
  campaignId: z.number().positive('캠페인을 선택해주세요'),
  domainCode: z.enum(['ESG', 'SAFETY', 'COMPLIANCE'], {
    errorMap: () => ({ message: '도메인을 선택해주세요' }),
  }),
  periodStartDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
  periodEndDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/),
});
```

---

## 7. UI 컴포넌트 가이드

### 7.1 상태 배지 컴포넌트

```tsx
// components/StatusBadge.tsx
interface StatusBadgeProps {
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

const STATUS_CONFIG = {
  PENDING: { label: '승인 대기중', color: 'yellow', bg: 'bg-yellow-100', text: 'text-yellow-800' },
  APPROVED: { label: '승인 완료', color: 'green', bg: 'bg-green-100', text: 'text-green-800' },
  REJECTED: { label: '승인 반려', color: 'red', bg: 'bg-red-100', text: 'text-red-800' },
};

export const StatusBadge = ({ status }: StatusBadgeProps) => {
  const config = STATUS_CONFIG[status];

  return (
    <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.bg} ${config.text}`}>
      {config.label}
    </span>
  );
};
```

### 7.2 역할 배지 컴포넌트

```tsx
// components/RoleBadge.tsx
const ROLE_CONFIG = {
  GUEST: { label: '게스트', color: 'gray' },
  DRAFTER: { label: '기안자', color: 'blue' },
  APPROVER: { label: '결재자', color: 'purple' },
  REVIEWER: { label: '수신자', color: 'orange' },
};

export const RoleBadge = ({ roleCode }: { roleCode: string }) => {
  const config = ROLE_CONFIG[roleCode as keyof typeof ROLE_CONFIG] || ROLE_CONFIG.GUEST;
  // ...
};
```

### 7.3 도메인 배지 컴포넌트

```tsx
// components/DomainBadge.tsx
const DOMAIN_CONFIG = {
  ESG: { label: 'ESG 실사', color: 'green', icon: '🌱' },
  SAFETY: { label: '안전보건', color: 'red', icon: '🦺' },
  COMPLIANCE: { label: '컴플라이언스', color: 'blue', icon: '📋' },
};

interface DomainBadgeProps {
  domainCode: 'ESG' | 'SAFETY' | 'COMPLIANCE';
  showIcon?: boolean;
}

export const DomainBadge = ({ domainCode, showIcon = true }: DomainBadgeProps) => {
  const config = DOMAIN_CONFIG[domainCode];

  return (
    <span className={`px-2 py-1 rounded text-xs font-medium bg-${config.color}-100 text-${config.color}-800`}>
      {showIcon && config.icon} {config.label}
    </span>
  );
};
```

### 7.4 도메인 선택 컴포넌트

```tsx
// components/DomainSelector.tsx
interface DomainSelectorProps {
  value: string | null;
  onChange: (domain: string) => void;
  userDomainRoles?: UserDomainRole[];
  filterByRole?: string;  // 특정 역할이 있는 도메인만 표시
}

export const DomainSelector = ({ value, onChange, userDomainRoles, filterByRole }: DomainSelectorProps) => {
  const availableDomains = userDomainRoles
    ?.filter(dr => !filterByRole || dr.roleCode === filterByRole)
    .map(dr => dr.domainCode) || ['ESG', 'SAFETY', 'COMPLIANCE'];

  return (
    <select value={value || ''} onChange={(e) => onChange(e.target.value)}>
      <option value="">전체 도메인</option>
      {availableDomains.map(code => (
        <option key={code} value={code}>
          {DOMAIN_CONFIG[code].label}
        </option>
      ))}
    </select>
  );
};
```

---

## 8. 페이지네이션 컴포넌트

```tsx
// components/Pagination.tsx
interface PaginationProps {
  page: PageDto;
  onPageChange: (page: number) => void;
}

export const Pagination = ({ page, onPageChange }: PaginationProps) => {
  const { number, totalPages } = page;

  return (
    <div className="flex items-center justify-center gap-2">
      <button
        onClick={() => onPageChange(number - 1)}
        disabled={number === 0}
      >
        이전
      </button>

      <span>{number + 1} / {totalPages}</span>

      <button
        onClick={() => onPageChange(number + 1)}
        disabled={number >= totalPages - 1}
      >
        다음
      </button>
    </div>
  );
};
```

---

## 9. 환경 변수 설정

### 9.1 .env 파일

```bash
# .env.development
VITE_API_URL=http://localhost:8080/api/v1

# .env.production
VITE_API_URL=https://api.esg-platform.com/api/v1
```

### 9.2 타입 정의

```typescript
// vite-env.d.ts
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

---

## 10. AI Run API 연동

### 10.1 AI Run API 타입 정의

```typescript
// types/aiRun.ts

interface AiPreviewRequest {
  fileIds: number[];
}

interface SlotStatus {
  slotName: string;
  required: boolean;
  submitted: boolean;
}

interface SlotHint {
  fileId: string;
  slotName: string;
  confidence?: number;
}

interface Clarification {
  targetSlot: string;
  code: string;
  message: string;
}

interface RunPreviewResponse {
  packageId: string;
  requiredSlotStatus: SlotStatus[];
  slotHints: SlotHint[];
  missingRequiredSlots: string[];  // 미제출 필수 슬롯 목록
}

interface SlotResult {
  slotName: string;
  status: 'VALID' | 'INVALID' | 'MISSING';
  message?: string;
  extractedData?: Record<string, any>;
}

interface AiAnalysisResultResponse {
  resultId: number;
  diagnosticId: number;
  domainCode: string;
  packageId: string;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  verdict: 'PASS' | 'WARN' | 'NEED_CLARIFY' | 'NEED_FIX';
  whySummary: string;
  resultJson: string;
  analyzedAt: string;
}
```

### 10.2 AI Run API 함수

```typescript
// api/aiRun.ts
import { apiClient } from './client';
import { BaseResponse } from '../types/api';

export const aiRunPreview = async (
  diagnosticId: number,
  fileIds: number[]
): Promise<RunPreviewResponse> => {
  const response = await apiClient.post<BaseResponse<RunPreviewResponse>>(
    `/ai/run/diagnostics/${diagnosticId}/preview`,
    { fileIds }
  );
  return response.data.data;
};

export const aiRunSubmit = async (diagnosticId: number): Promise<void> => {
  await apiClient.post(`/ai/run/diagnostics/${diagnosticId}/submit`);
};

export const getAiRunResult = async (
  diagnosticId: number
): Promise<AiAnalysisResultResponse> => {
  const response = await apiClient.get<BaseResponse<AiAnalysisResultResponse>>(
    `/ai/run/diagnostics/${diagnosticId}/result`
  );
  return response.data.data;
};

export const getAiRunHistory = async (
  diagnosticId: number
): Promise<AiAnalysisResultResponse[]> => {
  const response = await apiClient.get<BaseResponse<AiAnalysisResultResponse[]>>(
    `/ai/run/diagnostics/${diagnosticId}/history`
  );
  return response.data.data;
};
```

### 10.3 AI Run Custom Hooks

```typescript
// hooks/useAiRun.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import * as aiRunApi from '../api/aiRun';

export const QUERY_KEYS = {
  AI_RUN: {
    RESULT: (diagnosticId: number) => ['aiRun', 'result', diagnosticId] as const,
    HISTORY: (diagnosticId: number) => ['aiRun', 'history', diagnosticId] as const,
  },
};

export const useAiRunPreview = () => {
  return useMutation({
    mutationFn: ({ diagnosticId, fileIds }: { diagnosticId: number; fileIds: number[] }) =>
      aiRunApi.aiRunPreview(diagnosticId, fileIds),
  });
};

export const useAiRunSubmit = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (diagnosticId: number) => aiRunApi.aiRunSubmit(diagnosticId),
    onSuccess: (_, diagnosticId) => {
      // 제출 후 결과 polling 시작을 위해 무효화
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.AI_RUN.RESULT(diagnosticId) });
    },
  });
};

export const useAiRunResult = (diagnosticId: number, enabled = true) => {
  return useQuery({
    queryKey: QUERY_KEYS.AI_RUN.RESULT(diagnosticId),
    queryFn: () => aiRunApi.getAiRunResult(diagnosticId),
    enabled,
    refetchInterval: (query) => {
      // 결과가 아직 없으면(404 → error) 5초마다 재조회
      // NOTE: verdict에 'PENDING' 값은 없음. AI 분석 완료 전에는 result 자체가 없음(404).
      if (query?.state?.error || !query?.state?.data) {
        return 5000;
      }
      return false;
    },
    retry: false,  // 404는 polling으로 처리, retry 불필요
  });
};

export const useAiRunHistory = (diagnosticId: number) => {
  return useQuery({
    queryKey: QUERY_KEYS.AI_RUN.HISTORY(diagnosticId),
    queryFn: () => aiRunApi.getAiRunHistory(diagnosticId),
  });
};
```

### 10.4 AI Run 에러 핸들링

```typescript
// 에러 핸들러에 AI Run 에러 코드 추가
const ERROR_HANDLERS: Record<string, ErrorConfig> = {
  // ... 기존 에러 핸들러 ...

  // AI Run API 에러
  'AI001': { action: 'toast', customMessage: 'AI 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.' },
  'AI002': { action: 'toast', customMessage: '이미 분석이 진행 중입니다. 완료 후 다시 시도해주세요.' },
  'AI003': { action: 'toast', customMessage: 'AI 분석 결과가 없습니다. 먼저 분석을 요청해주세요.' },
};
```

---

## 11. 체크리스트

### API 연동 전 체크리스트

- [ ] BaseResponse 타입 정의 완료
- [ ] ErrorResponse 타입 정의 완료
- [ ] Axios interceptor 설정 완료
- [ ] 토큰 저장/갱신 로직 구현
- [ ] 에러 핸들러 구현

### 새 API 연동 시 체크리스트

- [ ] Request DTO 타입 정의
- [ ] Response DTO 타입 정의
- [ ] API 함수 작성
- [ ] Query Key 추가
- [ ] Custom Hook 작성
- [ ] 폼 검증 스키마 작성 (필요시)
- [ ] 에러 코드별 처리 확인

---

## 11. 참고 문서

- [API 명세서 v3.0](./API_SPECIFICATION_V3.md)
- [API Contract SSOT](./API_CONTRACT_SSOT.md)
