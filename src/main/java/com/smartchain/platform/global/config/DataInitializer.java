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
import com.smartchain.platform.domain.user.entity.Company;
import com.smartchain.platform.domain.user.entity.Industry;
import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.entity.User;
import com.smartchain.platform.domain.user.repository.CompanyRepository;
import com.smartchain.platform.domain.user.repository.IndustryRepository;
import com.smartchain.platform.domain.user.repository.RoleRepository;
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

@Slf4j
@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final IndustryRepository industryRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final DiagnosticRepository diagnosticRepository;
    private final ApprovalRepository approvalRepository;
    private final ReviewRepository reviewRepository;
    private final AsyncJobRepository asyncJobRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Role 데이터는 data.sql에서 초기화되므로, Role이 존재하는지 확인하는 로직은 제거하거나 수정할 수 있습니다.
        // 하지만 다른 데이터(User 등)가 이미 존재하는지 확인하는 것이 안전합니다.
        if (userRepository.count() > 0) {
            log.info("Data already initialized. Skipping...");
            return;
        }

        log.info("=== Starting Data Initialization ===");

        // 1. Role 조회 (data.sql에서 이미 생성됨)
        Role guestRole = roleRepository.findByCode("GUEST").orElseThrow(() -> new RuntimeException("Role not found: GUEST"));
        Role drafterRole = roleRepository.findByCode("DRAFTER").orElseThrow(() -> new RuntimeException("Role not found: DRAFTER"));
        Role approverRole = roleRepository.findByCode("APPROVER").orElseThrow(() -> new RuntimeException("Role not found: APPROVER"));
        Role reviewerRole = roleRepository.findByCode("REVIEWER").orElseThrow(() -> new RuntimeException("Role not found: REVIEWER"));
        log.info("Roles loaded: GUEST, DRAFTER, APPROVER, REVIEWER");

        // 2. Industry 생성
        Industry manufacturing = industryRepository.save(new Industry("제조업", "MANUFACTURING"));
        Industry it = industryRepository.save(new Industry("IT/소프트웨어", "IT"));
        Industry finance = industryRepository.save(new Industry("금융업", "FINANCE"));
        Industry retail = industryRepository.save(new Industry("유통/소매", "RETAIL"));
        Industry construction = industryRepository.save(new Industry("건설업", "CONSTRUCTION"));
        log.info("Industries created: 5 industries");

        // 3. Company 생성
        Company ownerCompany = companyRepository.save(Company.builder()
                .industry(manufacturing)
                .name("(주)스마트체인")
                .scale("대기업")
                .sales(500000000000L)
                .asset(1000000000000L)
                .businessNumber("123-45-67890")
                .companyType(CompanyType.TIER1)
                .ceoName("김대표")
                .address("서울시 강남구 테헤란로 123")
                .contactEmail("contact@smartchain.co.kr")
                .contactPhone("02-1234-5678")
                .status(CompanyStatus.ACTIVE)
                .build());

        Company partnerCompany1 = companyRepository.save(Company.builder()
                .industry(it)
                .name("(주)테크파트너")
                .scale("중견기업")
                .sales(50000000000L)
                .asset(100000000000L)
                .businessNumber("234-56-78901")
                .companyType(CompanyType.TIER2)
                .ceoName("이사장")
                .address("서울시 서초구 서초대로 456")
                .contactEmail("info@techpartner.co.kr")
                .contactPhone("02-2345-6789")
                .status(CompanyStatus.ACTIVE)
                .build());

        Company partnerCompany2 = companyRepository.save(Company.builder()
                .industry(manufacturing)
                .name("(주)그린매뉴")
                .scale("중소기업")
                .sales(10000000000L)
                .asset(20000000000L)
                .businessNumber("345-67-89012")
                .companyType(CompanyType.TIER2)
                .ceoName("박공장")
                .address("경기도 안산시 단원구 산단로 789")
                .contactEmail("contact@greenmanu.co.kr")
                .contactPhone("031-345-6789")
                .status(CompanyStatus.ACTIVE)
                .build());
        log.info("Companies created: 3 companies");

        // 4. User 생성 (비밀번호: Test1234!)
        String encodedPassword = passwordEncoder.encode("Test1234!");

        User reviewer = userRepository.save(User.builder()
                .name("심사자")
                .email("reviewer@smartchain.co.kr")
                .userPassword(encodedPassword)
                .company(ownerCompany)
                .role(reviewerRole)
                .build());

        User approver = userRepository.save(User.builder()
                .name("결재자")
                .email("approver@techpartner.co.kr")
                .userPassword(encodedPassword)
                .company(partnerCompany1)
                .role(approverRole)
                .build());

        User drafter1 = userRepository.save(User.builder()
                .name("기안자1")
                .email("drafter1@techpartner.co.kr")
                .userPassword(encodedPassword)
                .company(partnerCompany1)
                .role(drafterRole)
                .build());

        User drafter2 = userRepository.save(User.builder()
                .name("기안자2")
                .email("drafter2@greenmanu.co.kr")
                .userPassword(encodedPassword)
                .company(partnerCompany2)
                .role(drafterRole)
                .build());

        User guest = userRepository.save(User.builder()
                .name("게스트")
                .email("guest@example.com")
                .userPassword(encodedPassword)
                .company(partnerCompany1)
                .role(guestRole)
                .build());
        log.info("Users created: reviewer, approver, drafter1, drafter2, guest");

        // 5. Campaign 생성
        Campaign campaign2026 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-2026-001")
                .ownerCompanyId(ownerCompany.getCompanyId())
                .title("2026년 ESG 공급망 진단")
                .content("2026년도 협력사 ESG 경영 현황 진단 및 평가")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Campaign campaign2026Safety = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-2026-002")
                .ownerCompanyId(ownerCompany.getCompanyId())
                .title("2026년 상반기 안전보건 점검")
                .content("2026년 상반기 협력사 안전보건 관리 현황 점검")
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 6, 30))
                .deadline(LocalDate.of(2026, 2, 28))
                .build());

        Campaign campaign2026Compliance = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-2026-003")
                .ownerCompanyId(ownerCompany.getCompanyId())
                .title("2026년 하도급 컴플라이언스 점검")
                .content("2026년도 협력사 하도급 계약 및 법규 준수 현황 점검")
                .periodStartDate(LocalDate.of(2026, 3, 1))
                .periodEndDate(LocalDate.of(2026, 5, 31))
                .deadline(LocalDate.of(2026, 4, 30))
                .build());

        Campaign campaign2025 = campaignRepository.save(Campaign.builder()
                .campaignCode("CAMP-2025-001")
                .ownerCompanyId(ownerCompany.getCompanyId())
                .title("2025년 ESG 공급망 진단")
                .content("2025년도 협력사 ESG 경영 현황 진단 및 평가 (완료)")
                .periodStartDate(LocalDate.of(2025, 1, 1))
                .periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31))
                .build());

        log.info("Campaigns created: 4 campaigns (2026 ESG, 2026 Safety, 2026 Compliance, 2025 ESG)");

        // 6. Diagnostic 생성
        Diagnostic diagnostic1 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-2026-00001")
                .title("(주)테크파트너 2026년 ESG 진단")
                .campaign(campaign2026)
                .company(partnerCompany1)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        Diagnostic diagnostic2 = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-2026-00002")
                .title("(주)그린매뉴 2026년 ESG 진단")
                .campaign(campaign2026)
                .company(partnerCompany2)
                .drafterId(drafter2.getUserId())
                .periodStartDate(LocalDate.of(2026, 1, 1))
                .periodEndDate(LocalDate.of(2026, 12, 31))
                .deadline(LocalDate.of(2026, 3, 31))
                .build());

        // 진단1: 제출 상태로 변경
        diagnostic1.submit();
        diagnosticRepository.save(diagnostic1);
        log.info("Diagnostics created: 2 diagnostics");

        // 7. Approval 생성
        Approval approval1 = approvalRepository.save(Approval.builder()
                .diagnostic(diagnostic1)
                .requester(drafter1)
                .requestComment("ESG 진단 결과 검토 요청드립니다.")
                .deadline(LocalDate.of(2026, 2, 28))
                .build());
        log.info("Approval created: 1 approval (WAITING)");

        // 8. Review 생성 (승인된 진단에 대해서만)
        // diagnostic1을 승인 상태로 변경하고 Review 생성
        Diagnostic diagnosticForReview = diagnosticRepository.save(Diagnostic.builder()
                .diagnosticCode("DG-2026-00003")
                .title("(주)테크파트너 2025년 ESG 진단 (완료)")
                .campaign(campaign2026)
                .company(partnerCompany1)
                .drafterId(drafter1.getUserId())
                .periodStartDate(LocalDate.of(2025, 1, 1))
                .periodEndDate(LocalDate.of(2025, 12, 31))
                .deadline(LocalDate.of(2025, 3, 31))
                .build());
        diagnosticForReview.markAsApproved();
        diagnosticRepository.save(diagnosticForReview);

        Review review1 = reviewRepository.save(Review.builder()
                .diagnostic(diagnosticForReview)
                .company(partnerCompany1)
                .assignedReviewer(reviewer)
                .score(72)
                .submittedAt(LocalDateTime.now().minusDays(5))
                .build());
        log.info("Review created: 1 review (REVIEWING, score=72, MEDIUM risk)");

        // 9. AsyncJob 생성
        AsyncJob job1 = AsyncJob.builder()
                .jobType(JobType.FILE_PARSING)
                .requesterId(drafter1.getUserId())
                .targetId(diagnostic1.getDiagnosticId())
                .requestPayload("{\"fileId\": 1}")
                .estimatedSeconds(60)
                .build();
        job1.start();
        job1.succeed("/diagnostics/1/files/1/parsing-result");
        asyncJobRepository.save(job1);

        AsyncJob job2 = AsyncJob.builder()
                .jobType(JobType.REPORT_GENERATION)
                .requesterId(reviewer.getUserId())
                .targetId(review1.getReviewId())
                .requestPayload("{\"reviewId\": 1}")
                .estimatedSeconds(120)
                .build();
        asyncJobRepository.save(job2);
        log.info("AsyncJobs created: 2 jobs (1 SUCCEEDED, 1 PENDING)");

        // 10. Notification 생성
        notificationRepository.save(Notification.builder()
                .user(drafter1)
                .type(NotificationType.APPROVAL_COMPLETE)
                .title("ESG 진단 결재 완료")
                .message("(주)테크파트너 2025년 ESG 진단 결재가 승인되었습니다.")
                .linkUrl("/diagnostics/" + diagnosticForReview.getDiagnosticId())
                .build());

        notificationRepository.save(Notification.builder()
                .user(reviewer)
                .type(NotificationType.REVIEW_COMPLETE)
                .title("심사 대기 건 등록")
                .message("새로운 심사 건이 등록되었습니다: (주)테크파트너")
                .linkUrl("/reviews/" + review1.getReviewId())
                .build());

        notificationRepository.save(Notification.builder()
                .user(guest)
                .type(NotificationType.ROLE_APPROVED)
                .title("권한 승인 완료")
                .message("요청하신 기안자 권한이 승인되었습니다.")
                .linkUrl("/profile")
                .build());
        log.info("Notifications created: 3 notifications");

        log.info("=== Data Initialization Completed ===");
        log.info("Test accounts (password: Test1234!):");
        log.info("  - Reviewer: reviewer@smartchain.co.kr");
        log.info("  - Approver: approver@techpartner.co.kr");
        log.info("  - Drafter1: drafter1@techpartner.co.kr");
        log.info("  - Drafter2: drafter2@greenmanu.co.kr");
        log.info("  - Guest: guest@example.com");
    }
}
