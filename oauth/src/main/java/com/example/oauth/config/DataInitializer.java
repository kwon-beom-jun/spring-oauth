package com.example.oauth.config;

import com.example.oauth.entity.AuthRolesEntity;
import com.example.oauth.entity.AuthUsersEntity;
import com.example.oauth.repository.AuthRolesRepository;
import com.example.oauth.repository.AuthUsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 애플리케이션 시작 시점에 더미 유저와 롤을 생성하기 위한 설정
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AuthUsersRepository userRepository;
    private final AuthRolesRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // 예: ROLE_USER, ROLE_HRM_MANAGER, ROLE_RECEIPT_APPROVER 등 몇 개의 권한을 만들어 둠
            AuthRolesEntity roleUser = createRoleIfNotFound("ROLE_USER", "common");
            AuthRolesEntity roleHrmManager = createRoleIfNotFound("ROLE_HRM_MANAGER", "hrm");
            AuthRolesEntity roleReceiptApprover = createRoleIfNotFound("ROLE_RECEIPT_APPROVER", "receipt");

            // 이미 'testuser' 유저가 없다면 더미 유저 생성
            if(userRepository.findByUsername("testuser@example.com").isEmpty()) {
                AuthUsersEntity user = AuthUsersEntity.builder()
                        .username("testuser@example.com") // Email을 username처럼 사용
                        .password(passwordEncoder.encode("1234"))
                        .name("테스트유저")
                        .build();
                // 권한 할당 (여러 개 할당 가능)
                user.getRoles().add(roleUser);
                user.getRoles().add(roleHrmManager);

                userRepository.save(user);
            }

            // 추가로 다른 테스트 유저도 생성 가능
            if(userRepository.findByUsername("manager@example.com").isEmpty()) {
                AuthUsersEntity manager = AuthUsersEntity.builder()
                        .username("manager@example.com")
                        .password(passwordEncoder.encode("1234"))
                        .name("매니저유저")
                        .build();

                // 매니저 유저에게는 ROLE_USER, ROLE_HRM_MANAGER, ROLE_RECEIPT_APPROVER 모두 할당해보기
                manager.getRoles().add(roleUser);
                manager.getRoles().add(roleHrmManager);
                manager.getRoles().add(roleReceiptApprover);

                userRepository.save(manager);
            }
        };
    }

    private AuthRolesEntity createRoleIfNotFound(String roleName, String serviceName) {
        // Role_Name이 같은 경우가 있나 체크
        return roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst()
                .orElseGet(() -> {
                    AuthRolesEntity newRole = AuthRolesEntity.builder()
                            .roleName(roleName)
                            .serviceName(serviceName)
                            .build();
                    return roleRepository.save(newRole);
                });
    }
}
