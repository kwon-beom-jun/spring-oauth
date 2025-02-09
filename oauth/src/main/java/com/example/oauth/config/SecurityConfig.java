package com.example.oauth.config;

import com.example.oauth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * PasswordEncoder Bean 등록 (BCrypt 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider 설정
     * - CustomUserDetailsService와 PasswordEncoder 연동
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager Bean 등록
     * - 인증 시도 시 (일반 로그인) 사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain 설정
     * - HTTP 요청 보안을 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API 서버 시 주로 비활성화)
            .csrf(csrf -> csrf.disable())

            // 인증/인가 규칙 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 경로
                .requestMatchers("/auth/**", "/oauth2/**").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // 폼 로그인 설정
            .formLogin(form -> form
                // 커스텀 로그인 페이지 GET 매핑
                .loginPage("/auth/login-page").permitAll()
                // 로그인 성공 시 이동 URL
                .defaultSuccessUrl("/auth/success")
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                // 구글 로그인을 시도할 때도 동일한 커스텀 로그인 페이지
        		// 구글로 인증 시도 : /oauth2/authorization/google
            	// 카카오로 인증 시도 : /oauth2/authorization/kakao
            	// 인증 후 : /auth/oauth2/success
                .loginPage("/auth/login-page")
                // OAuth2 로그인 성공 시 이동 URL (강제)
                // 구글, 카카오 등 여러 소셜 로그인이 여기 통합
//                .defaultSuccessUrl("/auth/oauth2/success", true)
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("/auth/oauth2/success");
                })
//                .failureHandler((request, response, exception) -> {
//                    System.out.println("OAuth2 로그인 실패: " + exception.getMessage());
//                    response.sendRedirect("/auth/login-page?error=" + exception.getMessage());
//                })
            )

            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login-page")
                .permitAll()
            );

        return http.build();
    }
}
