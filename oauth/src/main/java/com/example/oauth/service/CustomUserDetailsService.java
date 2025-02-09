package com.example.oauth.service;

import com.example.oauth.entity.AuthRolesEntity;
import com.example.oauth.entity.AuthUsersEntity;
import com.example.oauth.repository.AuthUsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUsersRepository authUsersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AuthUsersEntity> optionalUser = authUsersRepository.findByUsername(username);
        AuthUsersEntity userEntity = optionalUser
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 1) 유저가 가진 Roles 가져오기
        Set<AuthRolesEntity> roles = userEntity.getRoles();

        // 2) 스프링 시큐리티용 Authority 리스트로 변환
        Set<GrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName())) // e.g. "ROLE_USER"
                .collect(Collectors.toSet());

        // 3) UserDetails 객체 생성
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .authorities(authorities) // 여러 권한 주입
                .build();
    }
}
