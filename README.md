
```
Backend
├─ .claude
│  ├─ rules
│  │  └─ troubleshooting.md
│  └─ settings.local.json
├─ CLAUDE.md
├─ docker-compose.yml
├─ Dockerfile
├─ docs
│  ├─ AI_INTEGRATION_GUIDE.md
│  ├─ API_CONTRACT_SSOT.md
│  ├─ API_QUICK_REFERENCE.md
│  ├─ API_SPECIFICATION.md
│  ├─ AZURE_DEPLOYMENT_GUIDE.md
│  ├─ BACKEND_IMPLEMENTATION_GUIDE.md
│  ├─ claude
│  │  └─ LEARNINGS.md
│  ├─ claude-workflow.md
│  ├─ E2E_TEST_SCENARIOS.md
│  ├─ ERD.md
│  ├─ ERD_DDL.sql
│  ├─ FE_INTEGRATION_RULES.md
│  ├─ logo.png
│  ├─ service_flow.md
│  ├─ service_flow.png
│  ├─ STATUS_AND_ERROR_CODES.md
│  ├─ TEST_DATA_GUIDE.md
│  └─ types
│     └─ api.types.ts
├─ gradle
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradlew
├─ gradlew.bat
├─ http
│  ├─ 00_seed_test_data.sql
│  ├─ 01_auth_and_setup.http
│  ├─ 02_esg_workflow.http
│  ├─ 03_safety_compliance_workflow.http
│  ├─ 04_ai_run_workflow.http
│  ├─ http-client.env.json
│  └─ README.md
├─ README123.md
├─ requirements.txt
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  └─ com
│  │  │     └─ smartchain
│  │  │        └─ platform
│  │  │           ├─ domain
│  │  │           │  ├─ ai
│  │  │           │  │  ├─ client
│  │  │           │  │  │  └─ AiRunApiClient.java
│  │  │           │  │  ├─ config
│  │  │           │  │  │  ├─ AiRunApiConfig.java
│  │  │           │  │  │  └─ SlotConfigProperties.java
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ AiAnalysisController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ AiAnalysisResult.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ AiAnalysisResultRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     ├─ AiAnalysisAsyncService.java
│  │  │           │  │     └─ AiAnalysisService.java
│  │  │           │  ├─ approval
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ ApprovalController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ Approval.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ ApprovalRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ ApprovalService.java
│  │  │           │  ├─ auth
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ AuthController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ EmailVerificationCode.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ EmailVerificationCodeRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     ├─ AuthService.java
│  │  │           │  │     ├─ EmailService.java
│  │  │           │  │     ├─ LocalEmailService.java
│  │  │           │  │     ├─ RecaptchaService.java
│  │  │           │  │     └─ SmtpEmailService.java
│  │  │           │  ├─ campaign
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ CampaignController.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ CampaignService.java
│  │  │           │  ├─ diagnostic
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ DiagnosticController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  ├─ Campaign.java
│  │  │           │  │  │  ├─ Category.java
│  │  │           │  │  │  ├─ DataPackage.java
│  │  │           │  │  │  ├─ Diagnostic.java
│  │  │           │  │  │  ├─ DiagnosticHistory.java
│  │  │           │  │  │  ├─ Question.java
│  │  │           │  │  │  ├─ Report.java
│  │  │           │  │  │  ├─ ResultQual.java
│  │  │           │  │  │  └─ ResultQuant.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  ├─ CampaignRepository.java
│  │  │           │  │  │  ├─ DiagnosticHistoryRepository.java
│  │  │           │  │  │  └─ DiagnosticRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ DiagnosticService.java
│  │  │           │  ├─ evidence
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ EvidenceFile.java
│  │  │           │  │  └─ repository
│  │  │           │  │     └─ EvidenceFileRepository.java
│  │  │           │  ├─ file
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ FileController.java
│  │  │           │  │  ├─ service
│  │  │           │  │  │  └─ FileService.java
│  │  │           │  │  └─ storage
│  │  │           │  │     ├─ AzureBlobStorageService.java
│  │  │           │  │     ├─ FileStorageService.java
│  │  │           │  │     └─ LocalFileStorageService.java
│  │  │           │  ├─ job
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ JobController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ AsyncJob.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ AsyncJobRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     ├─ AiAnalysisJobService.java
│  │  │           │  │     ├─ FileParsingJobService.java
│  │  │           │  │     ├─ JobService.java
│  │  │           │  │     └─ ReportExportService.java
│  │  │           │  ├─ log
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ ActivityLog.java
│  │  │           │  │  └─ repository
│  │  │           │  │     └─ ActivityLogRepository.java
│  │  │           │  ├─ management
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ ManagementController.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ ManagementService.java
│  │  │           │  ├─ notification
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ NotificationController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ Notification.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ NotificationRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ NotificationService.java
│  │  │           │  ├─ review
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ ReviewController.java
│  │  │           │  │  ├─ entity
│  │  │           │  │  │  └─ Review.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ ReviewRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ ReviewService.java
│  │  │           │  ├─ role
│  │  │           │  │  ├─ controller
│  │  │           │  │  │  └─ RoleRequestController.java
│  │  │           │  │  ├─ repository
│  │  │           │  │  │  └─ RoleRequestRepository.java
│  │  │           │  │  └─ service
│  │  │           │  │     └─ RoleRequestService.java
│  │  │           │  └─ user
│  │  │           │     ├─ controller
│  │  │           │     │  └─ DomainController.java
│  │  │           │     ├─ entity
│  │  │           │     │  ├─ Company.java
│  │  │           │     │  ├─ Domain.java
│  │  │           │     │  ├─ Industry.java
│  │  │           │     │  ├─ Role.java
│  │  │           │     │  ├─ RoleRequest.java
│  │  │           │     │  ├─ User.java
│  │  │           │     │  └─ UserDomainRole.java
│  │  │           │     ├─ repository
│  │  │           │     │  ├─ CompanyRepository.java
│  │  │           │     │  ├─ DomainRepository.java
│  │  │           │     │  ├─ IndustryRepository.java
│  │  │           │     │  ├─ RoleRepository.java
│  │  │           │     │  ├─ UserDomainRoleRepository.java
│  │  │           │     │  └─ UserRepository.java
│  │  │           │     └─ service
│  │  │           │        └─ DomainService.java
│  │  │           ├─ dto
│  │  │           │  ├─ ai
│  │  │           │  │  ├─ AiAnalysisRequest.java
│  │  │           │  │  ├─ AiAnalysisResultDetailResponse.java
│  │  │           │  │  ├─ AiAnalysisResultResponse.java
│  │  │           │  │  ├─ AiPreviewRequest.java
│  │  │           │  │  └─ run
│  │  │           │  │     ├─ Clarification.java
│  │  │           │  │     ├─ FileInfo.java
│  │  │           │  │     ├─ RunPreviewRequest.java
│  │  │           │  │     ├─ RunPreviewResponse.java
│  │  │           │  │     ├─ RunSubmitRequest.java
│  │  │           │  │     ├─ RunSubmitResponse.java
│  │  │           │  │     ├─ SlotHint.java
│  │  │           │  │     ├─ SlotResult.java
│  │  │           │  │     └─ SlotStatus.java
│  │  │           │  ├─ approval
│  │  │           │  │  ├─ common
│  │  │           │  │  │  ├─ ApprovalActionDto.java
│  │  │           │  │  │  ├─ ApprovalStatusDto.java
│  │  │           │  │  │  ├─ DiagnosticSimpleDto.java
│  │  │           │  │  │  ├─ PageDto.java
│  │  │           │  │  │  └─ RequesterDto.java
│  │  │           │  │  ├─ decision
│  │  │           │  │  │  ├─ ApprovalDecisionRequest.java
│  │  │           │  │  │  └─ ApprovalDecisionResponse.java
│  │  │           │  │  ├─ detail
│  │  │           │  │  │  ├─ ApprovalDetailResponse.java
│  │  │           │  │  │  ├─ DiagnosticDetailDto.java
│  │  │           │  │  │  ├─ ProcessedByDto.java
│  │  │           │  │  │  └─ RequesterDetailDto.java
│  │  │           │  │  ├─ download
│  │  │           │  │  │  ├─ BulkDownloadRequest.java
│  │  │           │  │  │  └─ BulkDownloadResponse.java
│  │  │           │  │  ├─ list
│  │  │           │  │  │  ├─ ApprovalListItemDto.java
│  │  │           │  │  │  ├─ ApprovalListRequest.java
│  │  │           │  │  │  ├─ ApprovalListResponse.java
│  │  │           │  │  │  └─ ApprovalStatsDto.java
│  │  │           │  │  ├─ submit
│  │  │           │  │  │  ├─ SubmitToReviewerRequest.java
│  │  │           │  │  │  └─ SubmitToReviewerResponse.java
│  │  │           │  │  └─ tab
│  │  │           │  │     ├─ AiReportTabResponse.java
│  │  │           │  │     ├─ ApprovalDetailTabDto.java
│  │  │           │  │     ├─ DataPackageCategoryDto.java
│  │  │           │  │     ├─ DataPackageFileItemDto.java
│  │  │           │  │     ├─ DataPackageTabResponse.java
│  │  │           │  │     ├─ DiagnosticPdfTabResponse.java
│  │  │           │  │     ├─ ModificationLogItemDto.java
│  │  │           │  │     ├─ ModificationLogTabRequest.java
│  │  │           │  │     └─ ModificationLogTabResponse.java
│  │  │           │  ├─ auth
│  │  │           │  │  ├─ common
│  │  │           │  │  │  ├─ CompanyInfoDto.java
│  │  │           │  │  │  ├─ DomainRoleDto.java
│  │  │           │  │  │  ├─ RoleInfoDto.java
│  │  │           │  │  │  └─ UserInfoDto.java
│  │  │           │  │  ├─ email
│  │  │           │  │  │  ├─ EmailCheckRequest.java
│  │  │           │  │  │  ├─ EmailCheckResponse.java
│  │  │           │  │  │  ├─ EmailVerificationRequest.java
│  │  │           │  │  │  ├─ EmailVerificationResponse.java
│  │  │           │  │  │  ├─ SendVerificationRequest.java
│  │  │           │  │  │  └─ SendVerificationResponse.java
│  │  │           │  │  ├─ login
│  │  │           │  │  │  ├─ LoginRequest.java
│  │  │           │  │  │  └─ LoginResponse.java
│  │  │           │  │  ├─ myinfo
│  │  │           │  │  │  ├─ MyDomainResponse.java
│  │  │           │  │  │  └─ MyInfoResponse.java
│  │  │           │  │  ├─ password
│  │  │           │  │  │  ├─ PasswordChangeRequest.java
│  │  │           │  │  │  └─ PasswordResetRequest.java
│  │  │           │  │  ├─ register
│  │  │           │  │  │  ├─ RegisterRequest.java
│  │  │           │  │  │  ├─ RegisterResponse.java
│  │  │           │  │  │  ├─ TermsAgreementItemDto.java
│  │  │           │  │  │  ├─ TermsAgreementRequest.java
│  │  │           │  │  │  ├─ TermsItemDto.java
│  │  │           │  │  │  └─ TermsListResponse.java
│  │  │           │  │  └─ token
│  │  │           │  │     ├─ TokenRefreshRequest.java
│  │  │           │  │     └─ TokenRefreshResponse.java
│  │  │           │  ├─ campaign
│  │  │           │  │  ├─ common
│  │  │           │  │  │  └─ PageDto.java
│  │  │           │  │  ├─ create
│  │  │           │  │  │  ├─ CampaignCreateRequest.java
│  │  │           │  │  │  └─ CampaignCreateResponse.java
│  │  │           │  │  ├─ dashboard
│  │  │           │  │  │  ├─ DashboardNoticeDto.java
│  │  │           │  │  │  ├─ DashboardResponse.java
│  │  │           │  │  │  ├─ DashboardStatsDto.java
│  │  │           │  │  │  ├─ DashboardTaskDto.java
│  │  │           │  │  │  └─ UserDashboardInfoDto.java
│  │  │           │  │  ├─ detail
│  │  │           │  │  │  ├─ CampaignDetailResponse.java
│  │  │           │  │  │  ├─ CampaignStatsDto.java
│  │  │           │  │  │  ├─ CampaignTargetCompanyDto.java
│  │  │           │  │  │  └─ CampaignTemplateDto.java
│  │  │           │  │  ├─ list
│  │  │           │  │  │  ├─ CampaignItemDto.java
│  │  │           │  │  │  └─ CampaignListResponse.java
│  │  │           │  │  └─ notification
│  │  │           │  │     ├─ NotificationItemDto.java
│  │  │           │  │     ├─ NotificationListResponse.java
│  │  │           │  │     ├─ NotificationReadRequest.java
│  │  │           │  │     ├─ NotificationReadResponse.java
│  │  │           │  │     ├─ NotificationSettingsResponse.java
│  │  │           │  │     └─ NotificationTypeSettingDto.java
│  │  │           │  ├─ common
│  │  │           │  │  ├─ api
│  │  │           │  │  │  ├─ ApiResponse.java
│  │  │           │  │  │  └─ FieldErrorDto.java
│  │  │           │  │  ├─ code
│  │  │           │  │  │  ├─ AccessRequestStatus.java
│  │  │           │  │  │  ├─ ApprovalStatus.java
│  │  │           │  │  │  ├─ DiagnosticStatus.java
│  │  │           │  │  │  ├─ ErrorCode.java
│  │  │           │  │  │  ├─ ReviewStatus.java
│  │  │           │  │  │  ├─ RiskLevel.java
│  │  │           │  │  │  └─ RoleCode.java
│  │  │           │  │  ├─ company
│  │  │           │  │  │  ├─ CompanyProfileDto.java
│  │  │           │  │  │  └─ CompanySimpleDto.java
│  │  │           │  │  ├─ file
│  │  │           │  │  │  ├─ DataPackageUrlResponse.java
│  │  │           │  │  │  ├─ EvidenceFileDto.java
│  │  │           │  │  │  ├─ FileDeleteResponse.java
│  │  │           │  │  │  ├─ FileDownloadUrlResponse.java
│  │  │           │  │  │  ├─ FileParsingResultResponse.java
│  │  │           │  │  │  ├─ FileUploadResponse.java
│  │  │           │  │  │  └─ PackageManifestDto.java
│  │  │           │  │  ├─ filter
│  │  │           │  │  │  ├─ DateRangeFilter.java
│  │  │           │  │  │  └─ SelectOption.java
│  │  │           │  │  ├─ job
│  │  │           │  │  │  ├─ JobErrorDto.java
│  │  │           │  │  │  ├─ JobResultDto.java
│  │  │           │  │  │  ├─ JobRetryResponse.java
│  │  │           │  │  │  └─ JobStatusResponse.java
│  │  │           │  │  └─ page
│  │  │           │  │     ├─ PagedResponse.java
│  │  │           │  │     ├─ PageDto.java
│  │  │           │  │     ├─ PageInfo.java
│  │  │           │  │     └─ PageRequest.java
│  │  │           │  ├─ diagnostic
│  │  │           │  │  ├─ ai
│  │  │           │  │  │  ├─ AiAnalysisResponse.java
│  │  │           │  │  │  ├─ CategoryScoresDto.java
│  │  │           │  │  │  ├─ HighlightDto.java
│  │  │           │  │  │  └─ RecommendationDto.java
│  │  │           │  │  ├─ common
│  │  │           │  │  │  ├─ CampaignSimpleDto.java
│  │  │           │  │  │  ├─ CompanySimpleDto.java
│  │  │           │  │  │  ├─ DomainSimpleDto.java
│  │  │           │  │  │  ├─ EvidenceFileDto.java
│  │  │           │  │  │  ├─ PageDto.java
│  │  │           │  │  │  ├─ PeriodDto.java
│  │  │           │  │  │  └─ ProgressDto.java
│  │  │           │  │  ├─ create
│  │  │           │  │  │  ├─ DiagnosticCreateRequest.java
│  │  │           │  │  │  └─ DiagnosticCreateResponse.java
│  │  │           │  │  ├─ detail
│  │  │           │  │  │  ├─ CampaignDetailDto.java
│  │  │           │  │  │  ├─ CreatedByDto.java
│  │  │           │  │  │  ├─ DiagnosticDetailFullResponse.java
│  │  │           │  │  │  └─ DiagnosticDetailResponse.java
│  │  │           │  │  ├─ history
│  │  │           │  │  │  ├─ DiagnosticHistoryItemDto.java
│  │  │           │  │  │  ├─ DiagnosticHistoryResponse.java
│  │  │           │  │  │  └─ PerformedByDto.java
│  │  │           │  │  ├─ list
│  │  │           │  │  │  ├─ DiagnosticListItemDto.java
│  │  │           │  │  │  ├─ DiagnosticListRequest.java
│  │  │           │  │  │  └─ DiagnosticListResponse.java
│  │  │           │  │  ├─ qualitative
│  │  │           │  │  │  ├─ AnswerOptionDto.java
│  │  │           │  │  │  ├─ CurrentAnswerDto.java
│  │  │           │  │  │  ├─ QualAnswerDto.java
│  │  │           │  │  │  ├─ QualAssessmentSaveRequest.java
│  │  │           │  │  │  ├─ QualAssessmentSaveResponse.java
│  │  │           │  │  │  ├─ QualCategoryDto.java
│  │  │           │  │  │  ├─ QualitativeAssessmentRequest.java
│  │  │           │  │  │  ├─ QualitativeAssessmentResponse.java
│  │  │           │  │  │  └─ QualQuestionItemDto.java
│  │  │           │  │  ├─ quantitative
│  │  │           │  │  │  ├─ CurrentValueDto.java
│  │  │           │  │  │  ├─ HistoricalValueDto.java
│  │  │           │  │  │  ├─ QuantAssessmentSaveRequest.java
│  │  │           │  │  │  ├─ QuantAssessmentSaveResponse.java
│  │  │           │  │  │  ├─ QuantCategoryDto.java
│  │  │           │  │  │  ├─ QuantIndicatorItemDto.java
│  │  │           │  │  │  ├─ QuantitativeAssessmentResponse.java
│  │  │           │  │  │  └─ QuantValueDto.java
│  │  │           │  │  ├─ report
│  │  │           │  │  │  ├─ AiReportInfoDto.java
│  │  │           │  │  │  ├─ ApprovalStatusCardDto.java
│  │  │           │  │  │  ├─ DataPackageFileDto.java
│  │  │           │  │  │  ├─ DataPackageInfoDto.java
│  │  │           │  │  │  ├─ DiagnosticReportPreviewResponse.java
│  │  │           │  │  │  ├─ ModificationLogDto.java
│  │  │           │  │  │  ├─ ReportTocIndicatorDto.java
│  │  │           │  │  │  ├─ ReportTocItemDto.java
│  │  │           │  │  │  └─ ReportTocSubItemDto.java
│  │  │           │  │  ├─ submit
│  │  │           │  │  │  ├─ DiagnosticSubmitErrorResponse.java
│  │  │           │  │  │  ├─ DiagnosticSubmitRequest.java
│  │  │           │  │  │  ├─ DiagnosticSubmitResponse.java
│  │  │           │  │  │  └─ DiagnosticSubmitStatusDto.java
│  │  │           │  │  └─ upload
│  │  │           │  │     ├─ DataSlotCategoryDto.java
│  │  │           │  │     ├─ DataSlotDto.java
│  │  │           │  │     ├─ DataStatusItemDto.java
│  │  │           │  │     ├─ DataUploadRequest.java
│  │  │           │  │     ├─ DataUploadResponse.java
│  │  │           │  │     ├─ DataUploadStatusDto.java
│  │  │           │  │     ├─ QuantitativeUploadPageResponse.java
│  │  │           │  │     ├─ StandardOptionDto.java
│  │  │           │  │     ├─ StandardSelectionRequest.java
│  │  │           │  │     ├─ StandardSelectionResponse.java
│  │  │           │  │     └─ UploadedFileInfoDto.java
│  │  │           │  ├─ domain
│  │  │           │  │  └─ DomainResponse.java
│  │  │           │  ├─ job
│  │  │           │  │  ├─ JobRetryResponse.java
│  │  │           │  │  └─ JobStatusResponse.java
│  │  │           │  ├─ management
│  │  │           │  │  ├─ common
│  │  │           │  │  │  ├─ CompanySimpleDto.java
│  │  │           │  │  │  ├─ PageDto.java
│  │  │           │  │  │  ├─ RoleSimpleDto.java
│  │  │           │  │  │  └─ UserSimpleDto.java
│  │  │           │  │  ├─ company
│  │  │           │  │  │  ├─ CompanyManagementItemDto.java
│  │  │           │  │  │  ├─ CompanyManagementRequest.java
│  │  │           │  │  │  ├─ CompanyManagementResponse.java
│  │  │           │  │  │  ├─ CompanyRegisterRequest.java
│  │  │           │  │  │  ├─ CompanyRegisterResponse.java
│  │  │           │  │  │  └─ IndustryInfoDto.java
│  │  │           │  │  ├─ kpi
│  │  │           │  │  │  ├─ EmissionFactorItemDto.java
│  │  │           │  │  │  ├─ EmissionFactorListResponse.java
│  │  │           │  │  │  ├─ KpiFormulaItemDto.java
│  │  │           │  │  │  └─ KpiFormulaListResponse.java
│  │  │           │  │  ├─ log
│  │  │           │  │  │  ├─ ActionInfoDto.java
│  │  │           │  │  │  ├─ ActivityLogExportRequest.java
│  │  │           │  │  │  ├─ ActivityLogExportResponse.java
│  │  │           │  │  │  ├─ ActivityLogFilterDto.java
│  │  │           │  │  │  ├─ ActivityLogItemDto.java
│  │  │           │  │  │  ├─ ActivityLogRequest.java
│  │  │           │  │  │  ├─ ActivityLogResponse.java
│  │  │           │  │  │  ├─ LogMetadataDto.java
│  │  │           │  │  │  ├─ TargetInfoDto.java
│  │  │           │  │  │  └─ UserLogInfoDto.java
│  │  │           │  │  ├─ permission
│  │  │           │  │  │  ├─ PermissionDashboardRequest.java
│  │  │           │  │  │  ├─ PermissionDashboardResponse.java
│  │  │           │  │  │  ├─ PermissionDecisionRequest.java
│  │  │           │  │  │  ├─ PermissionDecisionResponse.java
│  │  │           │  │  │  ├─ PermissionRequestDetailResponse.java
│  │  │           │  │  │  ├─ PermissionRequestItemDto.java
│  │  │           │  │  │  ├─ PermissionStatsDto.java
│  │  │           │  │  │  └─ RoleHistoryItemDto.java
│  │  │           │  │  ├─ prompt
│  │  │           │  │  │  ├─ PromptTemplateItemDto.java
│  │  │           │  │  │  └─ PromptTemplateListResponse.java
│  │  │           │  │  ├─ system
│  │  │           │  │  │  ├─ RetentionPolicyResponse.java
│  │  │           │  │  │  ├─ RetentionPolicyUpdateRequest.java
│  │  │           │  │  │  ├─ SystemSettingsResponse.java
│  │  │           │  │  │  └─ SystemSettingUpdateRequest.java
│  │  │           │  │  ├─ template
│  │  │           │  │  │  ├─ DiagnosticTemplateItemDto.java
│  │  │           │  │  │  └─ DiagnosticTemplateListResponse.java
│  │  │           │  │  └─ user
│  │  │           │  │     ├─ ChangedByDto.java
│  │  │           │  │     ├─ RoleChangeRequest.java
│  │  │           │  │     ├─ RoleChangeResponse.java
│  │  │           │  │     ├─ UserManagementItemDto.java
│  │  │           │  │     ├─ UserManagementRequest.java
│  │  │           │  │     ├─ UserManagementResponse.java
│  │  │           │  │     ├─ UserManagementStatsDto.java
│  │  │           │  │     ├─ UserStatusChangeRequest.java
│  │  │           │  │     └─ UserStatusChangeResponse.java
│  │  │           │  ├─ notification
│  │  │           │  │  ├─ NotificationItemDto.java
│  │  │           │  │  ├─ NotificationListResponse.java
│  │  │           │  │  ├─ NotificationReadRequest.java
│  │  │           │  │  ├─ NotificationReadResponse.java
│  │  │           │  │  └─ UnreadCountResponse.java
│  │  │           │  ├─ review
│  │  │           │  │  ├─ common
│  │  │           │  │  │  ├─ AssignedToDto.java
│  │  │           │  │  │  ├─ CompanySimpleDto.java
│  │  │           │  │  │  ├─ DiagnosticSimpleDto.java
│  │  │           │  │  │  ├─ PageDto.java
│  │  │           │  │  │  ├─ ProcessedByDto.java
│  │  │           │  │  │  └─ UserSimpleDto.java
│  │  │           │  │  ├─ dashboard
│  │  │           │  │  │  ├─ CategoryAveragesDto.java
│  │  │           │  │  │  ├─ OverviewDto.java
│  │  │           │  │  │  ├─ RecentActivityDto.java
│  │  │           │  │  │  ├─ ReviewDashboardRequest.java
│  │  │           │  │  │  ├─ ReviewDashboardResponse.java
│  │  │           │  │  │  └─ RiskDistributionDto.java
│  │  │           │  │  ├─ decision
│  │  │           │  │  │  ├─ ReviewDecisionRequest.java
│  │  │           │  │  │  ├─ ReviewDecisionResponse.java
│  │  │           │  │  │  ├─ RevisionRequestItemDto.java
│  │  │           │  │  │  ├─ RiskLevelUpdateRequest.java
│  │  │           │  │  │  └─ RiskLevelUpdateResponse.java
│  │  │           │  │  ├─ detail
│  │  │           │  │  │  ├─ ColumnOptionDto.java
│  │  │           │  │  │  ├─ CompanyDetailInfoDto.java
│  │  │           │  │  │  ├─ DiagnosticDetailInfoDto.java
│  │  │           │  │  │  ├─ FileInfoDto.java
│  │  │           │  │  │  ├─ ModificationReasonResponse.java
│  │  │           │  │  │  ├─ ReviewActionDto.java
│  │  │           │  │  │  ├─ ReviewDataCategoryDto.java
│  │  │           │  │  │  ├─ ReviewDataFileDto.java
│  │  │           │  │  │  ├─ ReviewDataPackageResponse.java
│  │  │           │  │  │  ├─ ReviewDetailResponse.java
│  │  │           │  │  │  ├─ ReviewDetailTabDto.java
│  │  │           │  │  │  ├─ ReviewHistoryItemDto.java
│  │  │           │  │  │  ├─ ReviewLogItemDto.java
│  │  │           │  │  │  ├─ ReviewModificationLogRequest.java
│  │  │           │  │  │  ├─ ReviewModificationLogResponse.java
│  │  │           │  │  │  └─ ReviewStatusCardDto.java
│  │  │           │  │  ├─ export
│  │  │           │  │  │  ├─ ExportFilterDto.java
│  │  │           │  │  │  ├─ ExportRequest.java
│  │  │           │  │  │  └─ ExportResponse.java
│  │  │           │  │  ├─ list
│  │  │           │  │  │  ├─ ReviewFileLinksDto.java
│  │  │           │  │  │  ├─ ReviewListItemDto.java
│  │  │           │  │  │  ├─ ReviewListRequest.java
│  │  │           │  │  │  ├─ ReviewListResponse.java
│  │  │           │  │  │  └─ ReviewSummaryDto.java
│  │  │           │  │  └─ report
│  │  │           │  │     ├─ BulkReportRequest.java
│  │  │           │  │     ├─ BulkReportResponse.java
│  │  │           │  │     ├─ ReportPublishRequest.java
│  │  │           │  │     └─ ReportPublishResponse.java
│  │  │           │  └─ role
│  │  │           │     ├─ approval
│  │  │           │     │  ├─ RoleApprovalDetailResponse.java
│  │  │           │     │  ├─ RoleApprovalItemDto.java
│  │  │           │     │  ├─ RoleApprovalListRequest.java
│  │  │           │     │  ├─ RoleApprovalListResponse.java
│  │  │           │     │  ├─ RoleDecisionRequest.java
│  │  │           │     │  └─ RoleDecisionResponse.java
│  │  │           │     ├─ common
│  │  │           │     │  ├─ CompanySimpleDto.java
│  │  │           │     │  ├─ DomainSimpleDto.java
│  │  │           │     │  ├─ PageDto.java
│  │  │           │     │  ├─ ProcessedByDto.java
│  │  │           │     │  ├─ RoleSimpleDto.java
│  │  │           │     │  └─ UserSimpleDto.java
│  │  │           │     ├─ history
│  │  │           │     │  └─ RoleChangeHistoryDto.java
│  │  │           │     ├─ page
│  │  │           │     │  ├─ CompanyOptionDto.java
│  │  │           │     │  ├─ DomainOptionDto.java
│  │  │           │     │  ├─ RoleOptionDto.java
│  │  │           │     │  └─ RoleRequestPageResponse.java
│  │  │           │     └─ request
│  │  │           │        ├─ RoleRequestCreateDto.java
│  │  │           │        ├─ RoleRequestResponse.java
│  │  │           │        └─ RoleRequestStatusDto.java
│  │  │           ├─ global
│  │  │           │  ├─ config
│  │  │           │  │  ├─ AsyncConfig.java
│  │  │           │  │  ├─ DataInitializer.java
│  │  │           │  │  ├─ JacksonConfig.java
│  │  │           │  │  ├─ JpaConfig.java
│  │  │           │  │  ├─ RecaptchaConfig.java
│  │  │           │  │  ├─ SecurityConfig.java
│  │  │           │  │  └─ SwaggerConfig.java
│  │  │           │  ├─ entity
│  │  │           │  │  └─ BaseTimeEntity.java
│  │  │           │  ├─ enums
│  │  │           │  │  ├─ ActionType.java
│  │  │           │  │  ├─ AiVerdict.java
│  │  │           │  │  ├─ ApprovalStatus.java
│  │  │           │  │  ├─ CompanyStatus.java
│  │  │           │  │  ├─ CompanyType.java
│  │  │           │  │  ├─ DiagnosticStatus.java
│  │  │           │  │  ├─ InputType.java
│  │  │           │  │  ├─ JobStatus.java
│  │  │           │  │  ├─ JobType.java
│  │  │           │  │  ├─ NotificationType.java
│  │  │           │  │  ├─ ParsingStatus.java
│  │  │           │  │  ├─ PipelinePhase.java
│  │  │           │  │  ├─ QuestionType.java
│  │  │           │  │  ├─ RequestStatus.java
│  │  │           │  │  ├─ ReviewStatus.java
│  │  │           │  │  ├─ RiskLevel.java
│  │  │           │  │  └─ UserStatus.java
│  │  │           │  ├─ error
│  │  │           │  │  ├─ CustomException.java
│  │  │           │  │  ├─ ErrorCode.java
│  │  │           │  │  └─ GlobalExceptionHandler.java
│  │  │           │  ├─ response
│  │  │           │  │  ├─ BaseResponse.java
│  │  │           │  │  └─ ErrorResponse.java
│  │  │           │  └─ security
│  │  │           │     ├─ DomainAuthorization.java
│  │  │           │     ├─ DomainAuthorizationAspect.java
│  │  │           │     ├─ JwtAuthenticationFilter.java
│  │  │           │     └─ JwtTokenProvider.java
│  │  │           ├─ HealthController.java
│  │  │           └─ PlatformApplication.java
│  │  └─ resources
│  │     ├─ application.yaml
│  │     └─ data.sql
│  └─ test
│     ├─ java
│     │  └─ com
│     │     └─ smartchain
│     │        └─ platform
│     │           ├─ docs
│     │           │  └─ ApiDocumentationConsistencyTest.java
│     │           ├─ domain
│     │           │  ├─ ai
│     │           │  │  ├─ client
│     │           │  │  │  └─ AiRunApiClientErrorHandlingTest.java
│     │           │  │  ├─ config
│     │           │  │  │  └─ SlotConfigPropertiesTest.java
│     │           │  │  ├─ service
│     │           │  │  │  └─ AiAnalysisServiceTest.java
│     │           │  │  └─ validation
│     │           │  │     └─ AiResponseValidationTest.java
│     │           │  ├─ approval
│     │           │  │  └─ service
│     │           │  │     └─ ApprovalServiceTest.java
│     │           │  ├─ auth
│     │           │  │  └─ service
│     │           │  │     ├─ AuthServiceTest.java
│     │           │  │     └─ EmailServiceTest.java
│     │           │  ├─ diagnostic
│     │           │  │  ├─ repository
│     │           │  │  │  ├─ CampaignRepositoryTest.java
│     │           │  │  │  └─ DiagnosticRepositoryTest.java
│     │           │  │  └─ service
│     │           │  │     └─ DiagnosticServiceTest.java
│     │           │  ├─ file
│     │           │  │  ├─ service
│     │           │  │  │  └─ FileServiceTest.java
│     │           │  │  └─ storage
│     │           │  │     └─ LocalFileStorageServiceTest.java
│     │           │  ├─ job
│     │           │  │  └─ service
│     │           │  │     ├─ AiAnalysisJobServiceTest.java
│     │           │  │     ├─ JobServiceTest.java
│     │           │  │     └─ ReportExportServiceTest.java
│     │           │  ├─ management
│     │           │  │  └─ service
│     │           │  │     └─ ManagementServiceTest.java
│     │           │  ├─ notification
│     │           │  │  └─ service
│     │           │  │     └─ NotificationServiceTest.java
│     │           │  ├─ review
│     │           │  │  ├─ repository
│     │           │  │  │  └─ ReviewRepositoryTest.java
│     │           │  │  └─ service
│     │           │  │     └─ ReviewServiceTest.java
│     │           │  ├─ role
│     │           │  │  └─ service
│     │           │  │     └─ RoleRequestServiceTest.java
│     │           │  └─ user
│     │           │     ├─ entity
│     │           │     │  └─ UserDomainPermissionTest.java
│     │           │     ├─ repository
│     │           │     │  └─ UserDomainRoleRepositoryTest.java
│     │           │     └─ service
│     │           │        └─ DomainServiceTest.java
│     │           ├─ dto
│     │           │  ├─ ai
│     │           │  │  └─ AiAnalysisResultDetailResponseTest.java
│     │           │  └─ diagnostic
│     │           │     └─ DiagnosticDtoTest.java
│     │           ├─ global
│     │           │  ├─ config
│     │           │  │  └─ JacksonConfigTest.java
│     │           │  ├─ enums
│     │           │  │  └─ PipelinePhaseTest.java
│     │           │  └─ security
│     │           │     └─ DomainAuthorizationAspectTest.java
│     │           ├─ integration
│     │           │  ├─ ApiSmokeTest.java
│     │           │  ├─ ApprovalIntegrationTest.java
│     │           │  ├─ DiagnosticIntegrationTest.java
│     │           │  ├─ DomainAuthorizationTest.java
│     │           │  └─ ReviewIntegrationTest.java
│     │           └─ PlatformApplicationTests.java
│     └─ resources
│        ├─ application-test.yaml
│        └─ schema.sql
└─ uploads
   └─ diagnostics
      └─ 3
         ├─ 144ec25f_TestCompany_202601_테스트메모.pdf
         ├─ 2675e216_TestCompany_202601_테스트메모.pdf
         ├─ 48f2798a_TestCompany_202601_테스트문서.pdf
         ├─ 573a8868_TestCompany_202601_테스트문서.pdf
         ├─ 5f1496b1_TestCompany_202601_테스트메모.pdf
         ├─ 86c7a739_test.pdf
         ├─ b633d3af_TestCompany_202601_테스트문서.pdf
         ├─ bd477f55_TestCompany_202601_테스트문서.pdf
         ├─ c06f6040_TestCompany_202601_테스트문서.pdf
         ├─ c1afecb3_TestCompany_202601_테스트문서.pdf
         ├─ e3e53511_TestCompany_202601_테스트메모.pdf
         └─ ea290913_TestCompany_202601_테스트메모.pdf

```