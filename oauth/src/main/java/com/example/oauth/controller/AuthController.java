package com.example.oauth.controller;

import com.example.oauth.entity.AuthUsersEntity;
import com.example.oauth.repository.AuthUsersRepository;
import com.example.oauth.service.JwtService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 인증을 처리하는 컨트롤러
 */
//@RestController
@Controller
@RequestMapping("/auth")
// final 붙은 항목은 생성자로 자동 생성해줌
// ⚡ 스프링 프레임워크는 @Autowired 없이도 final 필드를 포함한 생성자가 있으면 자동으로 의존성을 주입
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthUsersRepository authUsersRepository;

    /**
     * 일반 로그인 처리 (POST /auth/login)
     * - RequestBody: { "username": "?", "password": "?" }
     * - 성공 시 JWT 토큰 반환
     */
    @PostMapping("/login")
//    public @ResponseBody String login(@RequestBody LoginRequest request) {
	public @ResponseBody String login(
			/**
			 *	🔥 중요 🔥
			 * 		버전 @RequestParam String username 자동 인식	해결 방법
			 * 		Spring Boot 2.x (Java 8~11)	✅ 자동으로 매핑됨	추가 설정 필요 없음
			 * 		Spring Boot 3.x (Java 17)	❌ 자동 매핑 안됨 (파라미터 정보 사라짐)	@RequestParam("username") 명시 or -parameters 추가
			 * 		
			 * 		[ Gradle 설정 추가 예시 ]
			 * 		- 해당 기능 작동이 잘 되지 않아서 명시해서 사용했음
			 * 		tasks.withType(JavaCompile) {
			 * 			options.compilerArgs << "-parameters"  // ✅ 파라미터 이름 유지 설정 추가
			 * 		}
			 */
			@RequestParam("username") String username,
			@RequestParam("password") String password) {
    	
        // 사용자 인증 시도
    	// 1) 스프링 시큐리티 인증
    	/**
    	 *	순서
    	 *		UsernamePasswordAuthenticationToken 객체를 생성하여 전달
    	 *		→ AuthenticationManager는 AuthenticationProvider를 통해 유저 정보를 조회하고 인증 수행
    	 *		→ AuthenticationProvider가 UserDetailsService를 사용하여 DB에서 사용자 정보를 조회
    	 *		→ 내부적으로 CustomUserDetailsService의 loadUserByUsername() 메서드를 호출
    	 */
//        Authentication authentication = authenticationManager.authenticate( // authentication 변수를 사용하지 않음
    	authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
//                        request.getUsername(),
//                        request.getPassword()
                		username,
                		password
                )
        );
        
        // 3) DB에서 AuthUsersEntity 찾아오기
        AuthUsersEntity userEntity = authUsersRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));
        
        // 4) JWT 생성 (roles 포함)
        String jwt = jwtService.createToken(userEntity);
        
        // 콘솔에 토큰 찍어보기
        System.out.println("Generated JWT: " + jwt);

        // 응답으로 JWT 반환
        return "일반 로그인 성공! 발급된 JWT: " + jwt;
    }

    /**
     * 테스트용 로그인 페이지 (GET /auth/login-page)
     */
    @GetMapping("/login-page")
    public String loginPage() {
        // 실제 서비스라면 View를 반환하거나, 프론트엔드 페이지로 리다이렉트
        return "login"; // Thymeleaf에서 templates/login.html을 찾아 렌더링
    }

    /**
     * OAuth2 로그인 성공 시 이동 (GET /auth/oauth2/success)
     * - 여기서 JWT 토큰을 생성하여 콘솔에 찍어줍니다.
     * - 구글/카카오 모두 이곳으로 리다이렉트됨
     */
    @GetMapping("/oauth2/success")
    public @ResponseBody String oauth2LoginSuccess(Authentication authentication) {
    	
    	// OAuth2AuthenticationToken으로 다운캐스팅
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // 어느 Provider(google/kakao)인지 확인
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        
        // 인증 후 principal 정보(소셜 프로필 정보 꺼내기)
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        
        // userEntity를 가져오거나 없으면 가입 처리(DB 매핑용 사용자 엔티티)
        AuthUsersEntity userEntity;
        
        String email = null;
        String nickname = null;
        String profileImage = null;
        
        if ("google".equals(registrationId)) {
            // ************************
            // (A) 구글 로그인 처리
            // ************************
            // 구글은 보통 "sub"라는 고유 ID가 있음 / 구글 계정마다 변하지 않는 유니크 ID
            // authentication.getName() => sub 값이 들어오거나 이메일이 들어올 수도 있음 (설정에 따라 다름)

            String googleSubject = oAuth2User.getAttribute("sub"); // e.g. "1175547227..."
            email = oAuth2User.getAttribute("email");       // e.g. "xxxxx@gmail.com"
            nickname = (String) oAuth2User.getAttributes().get("name"); // 예) 구글 계정 이름
            profileImage = (String) oAuth2User.getAttributes().get("picture");

            // DB에서 유저 찾기
            Optional<AuthUsersEntity> optionalUser = authUsersRepository.findByUsername(email);
            
            if (optionalUser.isPresent()) {
                userEntity = optionalUser.get();
            } else {
                // 유저가 없으면 새로 가입
                AuthUsersEntity newUser = AuthUsersEntity.builder()
                        .username(email)
                        .password("GOOGLE_OAUTH")
                        .build();
                userEntity = authUsersRepository.save(newUser);
            }
            
            System.out.println("===============================");
            System.out.println("OAuth2 구글 로그인 성공! 발급된 JWT");
            System.out.println("googleSubject : " + googleSubject);
            System.out.println("email : " + email);
            System.out.println("nickname : " + nickname);
            System.out.println("profileImage : " + profileImage);
            System.out.println("===============================");
            
            // 필요 시 userEntity에 googleSubject 필드를 추가로 저장할 수도 있음

        } else if ("kakao".equals(registrationId)) {
            // ************************
            // (B) 카카오 로그인 처리
            // ************************
            // 카카오는 oAuth2User.getAttributes() 구조가 조금 다름
            // {id=1234567890, kakao_account={..., email=...}, properties={nickname=...} ...}
            Map<String, Object> attributes = oAuth2User.getAttributes();
            // 카카오 고유 id (Long형이거나 String으로 변환)
            String kakaoId = String.valueOf(attributes.get("id"));

            // 카카오 계정 정보
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email"); // 사용자가 동의한 경우만 내려옴
            }
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            if (properties != null) {
                nickname = (String) properties.get("nickname");
                profileImage = (String) properties.get("profile_image");
            }
            
            // 1) DB에서 email 기준으로 사용자 조회 (email이 null일 수도 있음!)
            //    혹은 kakaoId를 username으로 매핑할 수도 있음 (정책에 따라)
            if (email == null) {
                email = "kakaoUser_" + kakaoId;
            }

            // DB에서 유저 찾기
            Optional<AuthUsersEntity> userOptional = authUsersRepository.findByUsername(email);

            if (userOptional.isPresent()) {
                userEntity = userOptional.get();
            } else {
                // 유저가 없으면 새로 생성 후 저장
                userEntity = AuthUsersEntity.builder()
                        .username(email)
                        .password("KAKAO_OAUTH")
                        .build();
                userEntity = authUsersRepository.save(userEntity);
            }
            System.out.println("===============================");
            System.out.println("OAuth2 카카오 로그인 성공! 발급된 JWT");
            System.out.println("email : " + email);
            System.out.println("nickname : " + nickname);
            System.out.println("profileImage : " + profileImage);
            System.out.println("===============================");
        } else {
            // 그 외 다른 Provider면 에러 처리 or 확장 가능
            return "알 수 없는 소셜 로그인입니다. provider=" + registrationId;
        }
        
        // JWT 발급
        String jwt = jwtService.createToken(userEntity);
        // JWT 발급 (추가 정보도 넣고 싶으면 createToken 메서드 수정)
//        String jwt = jwtService.createTokenWithProfile(userEntity, nickname, profileImage);
        
        // 콘솔에 찍어 확인
        System.out.println("OAuth2 Login JWT: " + jwt);

        return "OAuth2 로그인 성공! 발급된 JWT: " + jwt;
    }
    
    /**
     * Google OAuth2 로그인 성공 후 JWT 발급
     * - Google OAuth2로 로그인한 후 이 API를 호출하면 JWT가 반환됨
     */
    @GetMapping("/token")
    public @ResponseBody String getToken(Authentication authentication) {
        if (authentication == null) {
            return "로그인되지 않았습니다.";
        }

        // 현재 로그인된 사용자 이름 가져오기
        String username = authentication.getName();

        // DB에서 AuthUsersEntity 조회 (username이 email이라면 그에 맞게 바꿔주세요)
        AuthUsersEntity userEntity = authUsersRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found in DB"));

        // JWT 생성
        String jwt = jwtService.createToken(userEntity);

        // 콘솔에 찍기
        System.out.println("OAuth2 Token Generated: " + jwt);

        return jwt; // 브라우저에서 확인 가능
    }

    /**
     * 로그인 요청 DTO
     */
    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
