package com.smartchain.platform.global.config;

import com.smartchain.platform.domain.approval.entity.Approval;
import com.smartchain.platform.domain.approval.repository.ApprovalRepository;
import com.smartchain.platform.domain.diagnostic.entity.Campaign;
import com.smartchain.platform.domain.diagnostic.entity.Diagnostic;
import com.smartchain.platform.domain.diagnostic.repository.CampaignRepository;
import com.smartchain.platform.domain.diagnostic.repository.DiagnosticRepository;
import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.domain.job.repository.AsyncJobRepository;
import com.smartchain.platform.domain.notification.entity.Notification;
import com.smartchain.platform.domain.notification.repository.NotificationRepository;
import com.smartchain.platform.domain.review.entity.Review;
import com.smartchain.platform.domain.review.repository.ReviewRepository;
import com.smartchain.platform.domain.role.repository.RoleRequestRepository;
import com.smartchain.platform.domain.user.entity.RoleRequest;
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Domain;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.entity.UserDomainRole;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.DomainRepository;
import com.smartchain.platform.domain.user.repository.IndustryRepository;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import com.smartchain.platform.domain.user.repository.UserDomainRoleRepository;
import com.smartchain.platform.domain.user.repository.UserRepository;
import com.smartchain.platform.global.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DomainRepository domainRepository;
    private final UserDomainRoleRepository userDomainRoleRepository;
    private final IndustryRepository industryRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final DiagnosticRepository diagnosticRepository;
    private final ApprovalRepository approvalRepository;
    private final ReviewRepository reviewRepository;
    private final AsyncJobRepository asyncJobRepository;
    private final NotificationRepository notificationRepository;
    private final RoleRequestRepository roleRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already initialized. Skipping...");
            return;
        }

        log.info("=== Starting Data Initialization ===");

        // ========================================
        // 1. 기본 데이터: Role, Domain, Industry
        // ========================================
        Role guestRole = roleRepository.findByCode("GUEST").orElseThrow(() -> new RuntimeException("Role not found: GUEST"));
        Role drafterRole = roleRepository.findByCode("DRAFTER").orElseThrow(() -> new RuntimeException("Role not found: DRAFTER"));
        Role approverRole = roleRepository.findByCode("APPROVER").orElseThrow(() -> new RuntimeException("Role not found: APPROVER"));
        Role reviewerRole = roleRepository.findByCode("REVIEWER").orElseThrow(() -> new RuntimeException("Role not found: REVIEWER"));
        log.info("Roles loaded: GUEST, DRAFTER, APPROVER, REVIEWER");

        Domain esgDomain = domainRepository.save(Domain.builder()
                .code("ESG").name("ESG 실사").description("ESG 공급망 실사 및 진단").isActive(true).build());
        Domain safetyDomain = domainRepository.save(Domain.builder()
                .code("SAFETY").name("안전보건").description("TBM 영상 분석 기반 안전보건 관리").isActive(true).build());
        Domain complianceDomain = domainRepository.save(Domain.builder()
                .code("COMPLIANCE").name("컴플라이언스").description("하도급 계약서 AI 검토").isActive(true).build());
        log.info("Domains created: ESG, SAFETY, COMPLIANCE");

        Industry manufacturing = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        Industry it = industryRepository.save(new Industry("IT/소프트웨어", "IT"));
        Industry construction = industryRepository.save(new Industry("건설업", "CONSTRUCTION"));
        industryRepository.save(new Industry("금융업", "FINANCE"));
        industryRepository.save(new Industry("유통/소매", "RETAIL"));
        log.info("Industries created: 5 industries");

        // ========================================
        // 2. 회사 데이터 (5개)
        // ========================================
        Company ownerCompany = companyRepository.save(Company.builder()
                .industry(manufacturing).name("HD현대중공업").scale("대기업")
                .sales(500000000000L).asset(1000000000000L).businessNumber("123-45-67890")
                .companyType(CompanyType.TIER1).ceoName("김대표")
                .address("울산광역시 동구 방어진순환도로 1000").contactEmail("contact@hdhhi.co.kr")
                .contactPhone("052-202-2114").status(CompanyStatus.ACTIVE).build());

        Company partnerCompany1 = companyRepository.save(Company.builder()
                .industry(it).name("(주)테크파트너").scale("중견기업")
                .sales(50000000000L).asset(100000000000L).businessNumber("234-56-78901")
                .companyType(CompanyType.TIER2).ceoName("이사장")
                .address("서울시 서초구 서초대로 456").contactEmail("info@techpartner.co.kr")
                .contactPhone("02-2345-6789").status(CompanyStatus.ACTIVE).build());

        Company partnerCompany2 = companyRepository.save(Company.builder()
                .industry(manufacturing).name("(주)그린매뉴팩처링").scale("중소기업")
                .sales(10000000000L).asset(20000000000L).businessNumber("345-67-89012")
                .companyType(CompanyType.TIER2).ceoName("박공장")
                .address("경기도 안산시 단원구 산단로 789").contactEmail("contact@greenmanu.co.kr")
                .contactPhone("031-345-6789").status(CompanyStatus.ACTIVE).build());

        Company partnerCompany3 = companyRepository.save(Company.builder()
                .industry(construction).name("(주)안전건설").scale("중견기업")
                .sales(30000000000L).asset(50000000000L).businessNumber("456-78-90123")
                .companyType(CompanyType.TIER2).ceoName("최건설")
                .address("부산시 해운대구 센텀중앙로 97").contactEmail("contact@safebuild.co.kr")
                .contactPhone("051-456-7890").status(CompanyStatus.ACTIVE).build());

        Company partnerCompany4 = companyRepository.save(Company.builder()
                .industry(manufacturing).name("(주)정밀부품").scale("중소기업")
                .sales(8000000000L).asset(15000000000L).businessNumber("567-89-01234")
                .companyType(CompanyType.TIER2).ceoName("정부품")
                .address("경남 창원시 성산구 공단로 123").contactEmail("contact@precision.co.kr")
                .contactPhone("055-567-8901").status(CompanyStatus.ACTIVE).build());
        log.info("Companies created: 5 companies");

        // ========================================
        // 3. 사용자 데이터 (12명)
        // ========================================
        String encodedPassword = passwordEncoder.encode("Test1234!");

        // 원청 심사자들 (3명 - 도메인별)
        User reviewerEsg = userRepository.save(User.builder()
                .name("김심사(ESG)").email("reviewer.esg@hdhhi.co.kr").userPassword(encodedPassword)
                .company(ownerCompany).role(reviewerRole).emailVerified(true).build());
        User reviewerSafety = userRepository.save(User.builder()
                .name("이심사(안전)").email("reviewer.safety@hdhhi.co.kr").userPassword(encodedPassword)
                .company(ownerCompany).role(reviewerRole).emailVerified(true).build());
        User reviewerCompliance = userRepository.save(User.builder()
                .name("박심사(컴플)").email("reviewer.compliance@hdhhi.co.kr").userPassword(encodedPassword)
                .company(ownerCompany).role(reviewerRole).emailVerified(true).build());

        // 테크파트너 직원들 (결재자1, 기안자2)
        User approver1 = userRepository.save(User.builder()
                .name("김결재(테크)").email("approver@techpartner.co.kr").userPassword(encodedPassword)
                .company(partnerCompany1).role(approverRole).emailVerified(true).build());
        User drafter1 = userRepository.save(User.builder()
                .name("이기안(테크)").email("drafter1@techpartner.co.kr").userPassword(encodedPassword)
                .company(partnerCompany1).role(drafterRole).emailVerified(true).build());
        User drafter2 = userRepository.save(User.builder()
                .name("박기안(테크)").email("drafter2@techpartner.co.kr").userPassword(encodedPassword)
                .company(partnerCompany1).role(drafterRole).emailVerified(true).build());

        // 그린매뉴팩처링 직원들
        User approver2 = userRepository.save(User.builder()
                .name("최결재(그린)").email("approver@greenmanu.co.kr").userPassword(encodedPassword)
                .company(partnerCompany2).role(approverRole).emailVerified(true).build());
        User drafter3 = userRepository.save(User.builder()
                .name("정기안(그린)").email("drafter@greenmanu.co.kr").userPassword(encodedPassword)
                .company(partnerCompany2).role(drafterRole).emailVerified(true).build());

        // 안전건설 직원들
        User approver3 = userRepository.save(User.builder()
                .name("강결재(안전)").email("approver@safebuild.co.kr").userPassword(encodedPassword)
                .company(partnerCompany3).role(approverRole).emailVerified(true).build());
        User drafter4 = userRepository.save(User.builder()
                .name("윤기안(안전)").email("drafter@safebuild.co.kr").userPassword(encodedPassword)
                .company(partnerCompany3).role(drafterRole).emailVerified(true).build());

        // 게스트 (신규 가입자)
        User guest1 = userRepository.save(User.builder()
                .name("신입1").email("newbie1@precision.co.kr").userPassword(encodedPassword)
                .company(partnerCompany4).role(guestRole).emailVerified(true).build());
        User guest2 = userRepository.save(User.builder()
                .name("신입2").email("newbie2@precision.co.kr").userPassword(encodedPassword)
                .company(partnerCompany4).role(guestRole).emailVerified(true).build());
        log.info("Users created: 12 users");

        // ========================================
        // 4. UserDomainRole (도메인별 권한)
        // ========================================
        // 심사자: 각자 담당 도메인
        userDomainRoleRepository.save(UserDomainRole.builder().user(reviewerEsg).domain(esgDomain).role(reviewerRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(reviewerSafety).domain(safetyDomain).role(reviewerRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(reviewerCompliance).domain(complianceDomain).role(reviewerRole).build());

        // 테크파트너: ESG(결재자+기안자) + COMPLIANCE(기안자만)
        userDomainRoleRepository.save(UserDomainRole.builder().user(approver1).domain(esgDomain).role(approverRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter1).domain(esgDomain).role(drafterRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter1).domain(complianceDomain).role(drafterRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter2).domain(esgDomain).role(drafterRole).build());

        // 그린매뉴팩처링: ESG(결재자+기안자) + SAFETY(기안자만)
        userDomainRoleRepository.save(UserDomainRole.builder().user(approver2).domain(esgDomain).role(approverRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter3).domain(esgDomain).role(drafterRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter3).domain(safetyDomain).role(drafterRole).build());

        // 안전건설: ESG(결재자) + SAFETY(기안자만) - APPROVER는 ESG 도메인에서만 존재
        userDomainRoleRepository.save(UserDomainRole.builder().user(approver3).domain(esgDomain).role(approverRole).build());
        userDomainRoleRepository.save(UserDomainRole.builder().user(drafter4).domain(safetyDomain).role(drafterRole).build());
        log.info("UserDomainRoles created");

        // ========================================
        // 5. Campaign (6개)
        // ========================================
        // 캠페인 기간: 4개월 단위 (1~4월, 5~8월, 9~12월)
        Campaign esgCampaign2026 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-ESG-2026-001").ownerCompanyId(ownerCompany.getCompanyId()).domain(esgDomain)
                .title("2026년 1분기 ESG 공급망 진단").content("2026년도 1차 협력사 ESG 경영 현황 진단")
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());

        Campaign esgCampaign2025 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-ESG-2025-001").ownerCompanyId(ownerCompany.getCompanyId()).domain(esgDomain)
                .title("2025년 3분기 ESG 공급망 진단 (완료)").content("2025년도 협력사 ESG 경영 현황 진단 - 완료")
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 11, 30)).build());

        Campaign safetyCampaign2026 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-SAFETY-2026-001").ownerCompanyId(ownerCompany.getCompanyId()).domain(safetyDomain)
                .title("2026년 1분기 안전보건 점검").content("2026년 1분기 협력사 안전보건 관리 현황 점검")
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());

        Campaign safetyCampaign2025 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-SAFETY-2025-002").ownerCompanyId(ownerCompany.getCompanyId()).domain(safetyDomain)
                .title("2025년 3분기 안전보건 점검 (완료)").content("2025년 3분기 안전보건 점검 완료")
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 11, 30)).build());

        Campaign complianceCampaign2026 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-COMPL-2026-001").ownerCompanyId(ownerCompany.getCompanyId()).domain(complianceDomain)
                .title("2026년 1분기 하도급 컴플라이언스 점검").content("2026년도 협력사 하도급 계약 법규 준수 점검")
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());

        Campaign complianceCampaign2025 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-COMPL-2025-001").ownerCompanyId(ownerCompany.getCompanyId()).domain(complianceDomain)
                .title("2025년 1분기 하도급 컴플라이언스 점검 (완료)").content("2025년도 하도급 점검 완료")
                .periodStartDate(LocalDate.of(2025, 1, 1)).periodEndDate(LocalDate.of(2025, 4, 30))
                .deadline(LocalDate.of(2025, 3, 31)).build());
        log.info("Campaigns created: 6 campaigns");

        // ========================================
        // 6. Diagnostic - 모든 상태 커버 (15개)
        // ========================================
        // ESG 진단들
        // WRITING (작성중) - 2건
        Diagnostic esgWriting1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-001").title("테크파트너 2026 ESG 진단")
                .campaign(esgCampaign2026).company(partnerCompany1).domain(esgDomain)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());

        Diagnostic esgWriting2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-002").title("그린매뉴 2026 ESG 진단")
                .campaign(esgCampaign2026).company(partnerCompany2).domain(esgDomain)
                .drafterId(drafter3.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());

        // SUBMITTED (제출됨) - 2건
        Diagnostic esgSubmitted1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-003").title("테크파트너 2026 ESG 진단 (제출)")
                .campaign(esgCampaign2026).company(partnerCompany1).domain(esgDomain)
                .drafterId(drafter2.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());
        esgSubmitted1.submit();
        diagnosticRepository.save(esgSubmitted1);

        Diagnostic esgSubmitted2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-004").title("정밀부품 2026 ESG 진단 (제출)")
                .campaign(esgCampaign2026).company(partnerCompany4).domain(esgDomain)
                .drafterId(guest1.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());
        esgSubmitted2.submit();
        diagnosticRepository.save(esgSubmitted2);

        // RETURNED (반려됨) - 1건
        Diagnostic esgReturned = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-005").title("그린매뉴 2026 ESG 진단 (반려)")
                .campaign(esgCampaign2026).company(partnerCompany2).domain(esgDomain)
                .drafterId(drafter3.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());
        esgReturned.submit();
        esgReturned.returnForRevision();
        diagnosticRepository.save(esgReturned);

        // APPROVED (승인됨, 심사대기) - 2건
        Diagnostic esgApproved1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-006").title("테크파트너 2026 ESG 진단 (승인)")
                .campaign(esgCampaign2026).company(partnerCompany1).domain(esgDomain)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());
        esgApproved1.submit();
        esgApproved1.approve();
        diagnosticRepository.save(esgApproved1);

        Diagnostic esgApproved2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2026-007").title("안전건설 2026 ESG 진단 (승인)")
                .campaign(esgCampaign2026).company(partnerCompany3).domain(esgDomain)
                .drafterId(drafter4.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 3, 31)).build());
        esgApproved2.submit();
        esgApproved2.approve();
        diagnosticRepository.save(esgApproved2);

        // REVIEWING (심사중) - 2건
        Diagnostic esgReviewing1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2025-001").title("테크파트너 2025 ESG 진단 (심사중)")
                .campaign(esgCampaign2025).company(partnerCompany1).domain(esgDomain)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31)).build());
        esgReviewing1.submit();
        esgReviewing1.approve();
        esgReviewing1.startReview();
        diagnosticRepository.save(esgReviewing1);

        Diagnostic esgReviewing2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2025-002").title("그린매뉴 2025 ESG 진단 (심사중)")
                .campaign(esgCampaign2025).company(partnerCompany2).domain(esgDomain)
                .drafterId(drafter3.getUserId())
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31)).build());
        esgReviewing2.submit();
        esgReviewing2.approve();
        esgReviewing2.startReview();
        diagnosticRepository.save(esgReviewing2);

        // COMPLETED (완료) - 2건
        Diagnostic esgCompleted1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2025-003").title("안전건설 2025 ESG 진단 (완료)")
                .campaign(esgCampaign2025).company(partnerCompany3).domain(esgDomain)
                .drafterId(drafter4.getUserId())
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31)).build());
        esgCompleted1.submit();
        esgCompleted1.approve();
        esgCompleted1.startReview();
        esgCompleted1.complete();
        diagnosticRepository.save(esgCompleted1);

        Diagnostic esgCompleted2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-ESG-2025-004").title("정밀부품 2025 ESG 진단 (완료)")
                .campaign(esgCampaign2025).company(partnerCompany4).domain(esgDomain)
                .drafterId(guest1.getUserId())
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31)).build());
        esgCompleted2.submit();
        esgCompleted2.approve();
        esgCompleted2.startReview();
        esgCompleted2.complete();
        diagnosticRepository.save(esgCompleted2);

        // SAFETY 진단 (3건)
        Diagnostic safetyWriting = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-SAFETY-2026-001").title("그린매뉴 2026 안전보건 점검")
                .campaign(safetyCampaign2026).company(partnerCompany2).domain(safetyDomain)
                .drafterId(drafter3.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 2, 28)).build());

        Diagnostic safetySubmitted = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-SAFETY-2026-002").title("안전건설 2026 안전보건 점검 (제출)")
                .campaign(safetyCampaign2026).company(partnerCompany3).domain(safetyDomain)
                .drafterId(drafter4.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 2, 28)).build());
        safetySubmitted.submit();
        diagnosticRepository.save(safetySubmitted);

        Diagnostic safetyCompleted = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-SAFETY-2025-001").title("안전건설 2025 안전보건 점검 (완료)")
                .campaign(safetyCampaign2025).company(partnerCompany3).domain(safetyDomain)
                .drafterId(drafter4.getUserId())
                .periodStartDate(LocalDate.of(2025, 9, 1)).periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 11, 30)).build());
        safetyCompleted.submit();
        safetyCompleted.approve();
        safetyCompleted.startReview();
        safetyCompleted.complete();
        diagnosticRepository.save(safetyCompleted);

        // COMPLIANCE 진단 (2건)
        Diagnostic complianceWriting = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-COMPL-2026-001").title("테크파트너 2026 컴플라이언스 점검")
                .campaign(complianceCampaign2026).company(partnerCompany1).domain(complianceDomain)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1)).periodEndDate(LocalDate.of(2026, 4, 30))
                .deadline(LocalDate.of(2026, 4, 15)).build());

        Diagnostic complianceCompleted = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-COMPL-2025-001").title("테크파트너 2025 컴플라이언스 점검 (완료)")
                .campaign(complianceCampaign2025).company(partnerCompany1).domain(complianceDomain)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2025, 1, 1)).periodEndDate(LocalDate.of(2025, 4, 30))
                .deadline(LocalDate.of(2025, 5, 15)).build());
        complianceCompleted.submit();
        complianceCompleted.approve();
        complianceCompleted.startReview();
        complianceCompleted.complete();
        diagnosticRepository.save(complianceCompleted);

        log.info("Diagnostics created: 17 diagnostics (all statuses covered)");

        // ========================================
        // 7. Approval - 모든 상태 커버 (6건)
        // ========================================
        // WAITING (대기중) - 2건
        approvalRepository.save(Approval.builder()
                .diagnostic(esgSubmitted1).requester(drafter2)
                .requestComment("ESG 진단 결과 검토 요청드립니다.")
                .deadline(LocalDate.of(2026, 3, 15)).build());

        approvalRepository.save(Approval.builder()
                .diagnostic(safetySubmitted).requester(drafter4)
                .requestComment("안전보건 점검 결과 결재 요청합니다.")
                .deadline(LocalDate.of(2026, 2, 20)).build());

        // APPROVED (승인) - 2건
        Approval approvedApproval1 = Approval.builder()
                .diagnostic(esgApproved1).requester(drafter1)
                .requestComment("ESG 진단 완료하여 결재 요청드립니다.")
                .deadline(LocalDate.of(2026, 3, 10)).build();
        approvedApproval1.approve(approver1, "검토 완료, 승인합니다.");
        approvalRepository.save(approvedApproval1);

        Approval approvedApproval2 = Approval.builder()
                .diagnostic(esgReviewing1).requester(drafter1)
                .requestComment("2025년 ESG 진단 결재 요청")
                .deadline(LocalDate.of(2025, 3, 20)).build();
        approvedApproval2.approve(approver1, "승인합니다.");
        approvalRepository.save(approvedApproval2);

        // REJECTED (반려) - 2건
        Approval rejectedApproval1 = Approval.builder()
                .diagnostic(esgReturned).requester(drafter3)
                .requestComment("ESG 진단 결재 요청드립니다.")
                .deadline(LocalDate.of(2026, 3, 1)).build();
        rejectedApproval1.reject(approver2, "온실가스 배출량 데이터가 누락되었습니다. 보완 후 재제출 바랍니다.");
        approvalRepository.save(rejectedApproval1);

        Approval rejectedApproval2 = Approval.builder()
                .diagnostic(esgSubmitted2).requester(guest1)
                .requestComment("ESG 진단 검토 부탁드립니다.")
                .deadline(LocalDate.of(2026, 3, 25)).build();
        // 이건 WAITING 상태로 유지 (게스트가 요청한 건)

        log.info("Approvals created: 5 approvals (WAITING: 2, APPROVED: 2, REJECTED: 1)");

        // ========================================
        // 8. Review - 모든 상태 커버 (6건)
        // ========================================
        // REVIEWING (심사중) - 2건
        Review reviewReviewing1 = reviewRepository.save(Review.builder()
                .diagnostic(esgReviewing1).company(partnerCompany1).domain(esgDomain)
                .assignedReviewer(reviewerEsg).score(68)
                .submittedAt(LocalDateTime.now().minusDays(3)).build());

        Review reviewReviewing2 = reviewRepository.save(Review.builder()
                .diagnostic(esgReviewing2).company(partnerCompany2).domain(esgDomain)
                .assignedReviewer(reviewerEsg).score(75)
                .submittedAt(LocalDateTime.now().minusDays(5)).build());

        // APPROVED (심사완료) - 2건
        Review reviewApproved1 = Review.builder()
                .diagnostic(esgCompleted1).company(partnerCompany3).domain(esgDomain)
                .assignedReviewer(reviewerEsg).score(82)
                .submittedAt(LocalDateTime.now().minusDays(30)).build();
        reviewApproved1 = reviewRepository.save(reviewApproved1);
        reviewApproved1.approve(reviewerEsg, "우수한 ESG 경영 현황입니다.",
                "환경관리 체계 우수", "안전보건 관리 양호", "지배구조 개선 필요");
        reviewRepository.save(reviewApproved1);

        Review reviewApproved2 = Review.builder()
                .diagnostic(safetyCompleted).company(partnerCompany3).domain(safetyDomain)
                .assignedReviewer(reviewerSafety).score(91)
                .submittedAt(LocalDateTime.now().minusDays(45)).build();
        reviewApproved2 = reviewRepository.save(reviewApproved2);
        reviewApproved2.approve(reviewerSafety, "안전보건 관리 체계 우수합니다.", null, null, null);
        reviewRepository.save(reviewApproved2);

        // REVISION_REQUIRED (보완요청) - 2건
        Review reviewRevision1 = Review.builder()
                .diagnostic(esgCompleted2).company(partnerCompany4).domain(esgDomain)
                .assignedReviewer(reviewerEsg).score(45)
                .submittedAt(LocalDateTime.now().minusDays(20)).build();
        reviewRevision1 = reviewRepository.save(reviewRevision1);
        reviewRevision1.requestRevision(reviewerEsg, "환경 부문 증빙자료가 부족합니다. 에너지 사용량 증빙 보완 필요.");
        reviewRepository.save(reviewRevision1);

        Review reviewRevision2 = Review.builder()
                .diagnostic(complianceCompleted).company(partnerCompany1).domain(complianceDomain)
                .assignedReviewer(reviewerCompliance).score(58)
                .submittedAt(LocalDateTime.now().minusDays(60)).build();
        reviewRevision2 = reviewRepository.save(reviewRevision2);
        reviewRevision2.requestRevision(reviewerCompliance, "하도급 계약서 일부 조항 검토 필요. 대금지급 조건 명시 보완 요청.");
        reviewRepository.save(reviewRevision2);

        log.info("Reviews created: 6 reviews (REVIEWING: 2, APPROVED: 2, REVISION_REQUIRED: 2)");

        // ========================================
        // 9. RoleRequest - 모든 상태 커버 (4건)
        // ========================================
        // PENDING (대기중) - 2건
        roleRequestRepository.save(RoleRequest.builder()
                .user(guest1).company(partnerCompany4).domain(esgDomain)
                .requestedRole("DRAFTER").reason("ESG 진단 업무 담당자로 지정되었습니다.")
                .status(RequestStatus.PENDING).build());

        roleRequestRepository.save(RoleRequest.builder()
                .user(guest2).company(partnerCompany4).domain(safetyDomain)
                .requestedRole("DRAFTER").reason("안전보건 점검 업무를 담당하게 되었습니다.")
                .status(RequestStatus.PENDING).build());

        // APPROVED (승인) - 1건
        RoleRequest approvedRequest = RoleRequest.builder()
                .user(drafter1).company(partnerCompany1).domain(complianceDomain)
                .requestedRole("DRAFTER").reason("컴플라이언스 업무도 담당하게 되었습니다.")
                .status(RequestStatus.PENDING).build();
        approvedRequest.approve(reviewerCompliance);
        roleRequestRepository.save(approvedRequest);

        // REJECTED (거절) - 1건
        RoleRequest rejectedRequest = RoleRequest.builder()
                .user(guest2).company(partnerCompany4).domain(esgDomain)
                .requestedRole("APPROVER").reason("결재자 권한이 필요합니다.")
                .status(RequestStatus.PENDING).build();
        rejectedRequest.reject(reviewerEsg, "현재 기안자 권한만 부여 가능합니다. 담당자 확인 후 재요청 바랍니다.");
        roleRequestRepository.save(rejectedRequest);

        log.info("RoleRequests created: 4 requests (PENDING: 2, APPROVED: 1, REJECTED: 1)");

        // ========================================
        // 10. AsyncJob (5건)
        // ========================================
        AsyncJob jobSucceeded = AsyncJob.builder()
                .jobType(JobType.FILE_PARSING).requesterId(drafter1.getUserId())
                .targetId(esgSubmitted1.getDiagnosticId())
                .requestPayload("{\"fileId\": 1}").estimatedSeconds(60).build();
        jobSucceeded.start();
        jobSucceeded.succeed("/api/v1/diagnostics/" + esgSubmitted1.getDiagnosticId() + "/files/1/result");
        asyncJobRepository.save(jobSucceeded);

        AsyncJob jobPending = AsyncJob.builder()
                .jobType(JobType.REPORT_GENERATION).requesterId(reviewerEsg.getUserId())
                .targetId(reviewReviewing1.getReviewId())
                .requestPayload("{\"reviewId\": " + reviewReviewing1.getReviewId() + "}").estimatedSeconds(120).build();
        asyncJobRepository.save(jobPending);

        AsyncJob jobRunning = AsyncJob.builder()
                .jobType(JobType.AI_ESG_ANALYSIS).requesterId(drafter3.getUserId())
                .targetId(esgWriting2.getDiagnosticId())
                .requestPayload("{\"diagnosticId\": " + esgWriting2.getDiagnosticId() + "}").estimatedSeconds(180).build();
        jobRunning.start();
        asyncJobRepository.save(jobRunning);

        AsyncJob jobFailed = AsyncJob.builder()
                .jobType(JobType.FILE_PARSING).requesterId(drafter4.getUserId())
                .targetId(safetySubmitted.getDiagnosticId())
                .requestPayload("{\"fileId\": 99}").estimatedSeconds(60).build();
        jobFailed.start();
        jobFailed.fail("FILE_NOT_FOUND", "요청한 파일을 찾을 수 없습니다.", false);
        asyncJobRepository.save(jobFailed);

        AsyncJob jobBulk = AsyncJob.builder()
                .jobType(JobType.BULK_REPORT).requesterId(reviewerEsg.getUserId())
                .targetId(esgCampaign2025.getCampaignId())
                .requestPayload("{\"campaignId\": " + esgCampaign2025.getCampaignId() + "}").estimatedSeconds(300).build();
        jobBulk.start();
        jobBulk.succeed("/api/v1/reports/bulk/campaign-" + esgCampaign2025.getCampaignId() + ".zip");
        asyncJobRepository.save(jobBulk);

        log.info("AsyncJobs created: 5 jobs (PENDING: 1, RUNNING: 1, SUCCEEDED: 2, FAILED: 1)");

        // ========================================
        // 11. Notification (다양한 타입, 10건)
        // ========================================
        // 결재 관련
        notificationRepository.save(Notification.builder()
                .user(approver1).type(NotificationType.APPROVAL_COMPLETE)
                .title("결재 요청").message("테크파트너 2026 ESG 진단 결재 요청이 도착했습니다.")
                .linkUrl("/approvals").build());

        notificationRepository.save(Notification.builder()
                .user(drafter2).type(NotificationType.APPROVAL_COMPLETE)
                .title("결재 완료").message("ESG 진단 결재가 승인되었습니다.")
                .linkUrl("/diagnostics/" + esgApproved1.getDiagnosticId()).build());

        notificationRepository.save(Notification.builder()
                .user(drafter3).type(NotificationType.REVISION_REQUIRED)
                .title("결재 반려").message("ESG 진단이 반려되었습니다. 사유: 온실가스 배출량 데이터 누락")
                .linkUrl("/diagnostics/" + esgReturned.getDiagnosticId()).build());

        // 심사 관련
        notificationRepository.save(Notification.builder()
                .user(reviewerEsg).type(NotificationType.REVIEW_COMPLETE)
                .title("심사 요청").message("새로운 ESG 심사 건이 등록되었습니다: 테크파트너")
                .linkUrl("/reviews/" + reviewReviewing1.getReviewId()).build());

        notificationRepository.save(Notification.builder()
                .user(drafter1).type(NotificationType.REVIEW_COMPLETE)
                .title("심사 완료").message("ESG 심사가 완료되었습니다. 등급: B")
                .linkUrl("/diagnostics/" + esgCompleted1.getDiagnosticId()).build());

        // 역할 관련
        notificationRepository.save(Notification.builder()
                .user(guest1).type(NotificationType.ROLE_APPROVED)
                .title("권한 요청 접수").message("DRAFTER 권한 요청이 접수되었습니다.")
                .linkUrl("/profile").build());

        notificationRepository.save(Notification.builder()
                .user(drafter1).type(NotificationType.ROLE_APPROVED)
                .title("권한 승인").message("컴플라이언스 도메인 DRAFTER 권한이 승인되었습니다.")
                .linkUrl("/profile").build());

        notificationRepository.save(Notification.builder()
                .user(guest2).type(NotificationType.REVISION_REQUIRED)
                .title("권한 거절").message("APPROVER 권한 요청이 거절되었습니다.")
                .linkUrl("/profile").build());

        // AI 분석 관련
        notificationRepository.save(Notification.builder()
                .user(drafter1).type(NotificationType.AI_ANALYSIS_COMPLETE)
                .title("AI 분석 완료").message("ESG 진단 AI 분석이 완료되었습니다.")
                .linkUrl("/diagnostics/" + esgSubmitted1.getDiagnosticId()).build());

        notificationRepository.save(Notification.builder()
                .user(drafter4).type(NotificationType.AI_ANALYSIS_FAILED)
                .title("AI 분석 실패").message("파일 분석에 실패했습니다. 파일을 확인해주세요.")
                .linkUrl("/diagnostics/" + safetySubmitted.getDiagnosticId()).build());

        log.info("Notifications created: 10 notifications (various types)");

        // ========================================
        // 완료 로그
        // ========================================
        log.info("=== Data Initialization Completed ===");
        log.info("");
        log.info("============================================");
        log.info("        TEST ACCOUNTS (password: Test1234!)");
        log.info("============================================");
        log.info("");
        log.info("[원청 심사자 - REVIEWER]");
        log.info("  ESG:        reviewer.esg@hdhhi.co.kr");
        log.info("  안전보건:   reviewer.safety@hdhhi.co.kr");
        log.info("  컴플라이언스: reviewer.compliance@hdhhi.co.kr");
        log.info("");
        log.info("[협력사 결재자 - APPROVER]");
        log.info("  테크파트너: approver@techpartner.co.kr (ESG, COMPLIANCE)");
        log.info("  그린매뉴:   approver@greenmanu.co.kr (ESG, SAFETY)");
        log.info("  안전건설:   approver@safebuild.co.kr (SAFETY)");
        log.info("");
        log.info("[협력사 기안자 - DRAFTER]");
        log.info("  테크파트너: drafter1@techpartner.co.kr, drafter2@techpartner.co.kr");
        log.info("  그린매뉴:   drafter@greenmanu.co.kr");
        log.info("  안전건설:   drafter@safebuild.co.kr");
        log.info("");
        log.info("[게스트 - GUEST]");
        log.info("  정밀부품:   newbie1@precision.co.kr, newbie2@precision.co.kr");
        log.info("============================================");
    }
}
