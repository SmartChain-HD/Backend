package com.smartchain.platform.global.config;

import com.smartchain.platform.domain.user.entity.Role;
import com.smartchain.platform.domain.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {

    private static final List<RoleSeed> DEFAULT_ROLES = List.of(
            new RoleSeed("Guest", "GUEST"),
            new RoleSeed("Drafter", "DRAFTER"),
            new RoleSeed("Approver", "APPROVER"),
            new RoleSeed("Reviewer", "REVIEWER")
    );

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long count = roleRepository.count();
        if (count == 0) {
            log.warn("role table is empty. Seeding default roles.");
            DEFAULT_ROLES.forEach(this::insertIfMissing);
            log.info("Default roles seeded. currentCount={}", roleRepository.count());
            return;
        }

        // Keep startup idempotent even if a subset is missing.
        DEFAULT_ROLES.forEach(this::insertIfMissing);
    }

    private void insertIfMissing(RoleSeed seed) {
        if (roleRepository.existsByCode(seed.code())) {
            return;
        }
        roleRepository.save(new Role(seed.name(), seed.code()));
        log.info("Inserted missing role: {}", seed.code());
    }

    private record RoleSeed(String name, String code) {
    }
}
