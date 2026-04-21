package com.maayn.iamservice.config;
import com.maayn.iamservice.domain.entity.Role;
import com.maayn.iamservice.domain.entity.User;
import com.maayn.iamservice.repository.RoleRepository;
import com.maayn.iamservice.repository.UserRepository;
import com.maayn.iamservice.security.PasswordHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import java.util.UUID;
/**
 * Seeds the canonical roles and a bootstrap super-admin account on startup.
 *
 * The super-admin credentials are taken from the following properties /
 * environment variables (with sensible defaults for dev only):
 *   iam.superadmin.username / IAM_SUPERADMIN_USERNAME
 *   iam.superadmin.email    / IAM_SUPERADMIN_EMAIL
 *   iam.superadmin.password / IAM_SUPERADMIN_PASSWORD
 *
 * The seeder is idempotent: it creates the roles and the user only if they
 * do not yet exist, and tops-up the role set of an existing super-admin user
 * so they always have the SUPERADMIN role.
 */
@Configuration
public class DataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    /** Canonical set of roles recognised by the system. */
    private static final List<String> DEFAULT_ROLES =
            List.of("SUPERADMIN", "ADMIN", "EMPLOYEE", "CUSTOMER");
    /**
     * Well-known fixed UUID for the bootstrap superadmin.
     * Shared with account-service and transaction-service so demo accounts can be
     * linked to this user without any inter-service calls at startup.
     */
    public static final UUID SUPERADMIN_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    ApplicationRunner iamDataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordHashService passwordHashService,
            PlatformTransactionManager txManager,
            @Value("${iam.superadmin.username:superadmin}") String adminUsername,
            @Value("${iam.superadmin.email:superadmin@aetherbank.local}") String adminEmail,
            @Value("${iam.superadmin.password:Password1!}") String adminPassword
    ) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return args -> tx.executeWithoutResult(status -> seed(
                roleRepository, userRepository, passwordHashService,
                adminUsername, adminEmail, adminPassword));
    }

    private void seed(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordHashService passwordHashService,
                      String adminUsername,
                      String adminEmail,
                      String adminPassword) {
        // 1) Ensure canonical roles exist.
        for (String name : DEFAULT_ROLES) {
            roleRepository.findByName(name).orElseGet(() -> {
                log.info("Seeding role: {}", name);
                return roleRepository.save(Role.builder().name(name).build());
            });
        }
        Role superadminRole = roleRepository.findByName("SUPERADMIN")
                .orElseThrow(() -> new IllegalStateException("SUPERADMIN role missing after seeding"));

        // 2) Ensure the bootstrap super-admin exists with the well-known fixed UUID
        //    so other services can reference it without inter-service calls.
        User admin = userRepository.findById(SUPERADMIN_ID)
                .or(() -> userRepository.findByUsername(adminUsername))
                .or(() -> userRepository.findByEmail(adminEmail))
                .orElse(null);

        if (admin == null) {
            admin = User.builder()
                    .id(SUPERADMIN_ID)
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordHashService.hash(adminPassword))
                    .fullName("Super Admin")
                    .isActive(true)
                    .isEmailVerified(true)
                    .mfaEnabled(false)
                    .build();
            admin.addRole(superadminRole);
            userRepository.save(admin);
            log.warn("Seeded bootstrap SUPERADMIN '{}' id={} ({}). Change the password in production!",
                    adminUsername, SUPERADMIN_ID, adminEmail);
        } else {
            if (admin.getRoles().stream().noneMatch(r -> "SUPERADMIN".equalsIgnoreCase(r.getName()))) {
                admin.addRole(superadminRole);
                userRepository.save(admin);
                log.info("Granted SUPERADMIN role to existing user {}", admin.getUsername());
            }
        }
    }
}