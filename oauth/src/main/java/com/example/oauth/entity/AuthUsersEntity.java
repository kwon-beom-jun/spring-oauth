package com.example.oauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * auth_users 테이블 매핑 엔티티
 */
@Entity
@Table(name = "auth_users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsersEntity {

    @Id
    @Column(name = "User_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    // DB 컬럼 Password와 매핑 (Bcrypt로 암호화된 패스워드)
    @Column(name = "Password", nullable = false)
    private String password;

    // username으로 사용할 컬럼이 DB에 없다면, Email이나 Name 등을 써도 좋음
    // 여기서는 username 역할을 할 별도 필드 설정(예: Email = 고유 식별자)
    @Column(name = "Email", unique = true)
    private String username;

    // 예시로 Name 칼럼도 매핑
    @Column(name = "Name")
    private String name;

    // ... 그 외 Birth, Phone_Number, Position_ID 등 필요 시 매핑
    // 생략 가능

    /**
     *	다대다 관계 설정 (유저 - 롤)
     *	Set<RoleEntity>를 통해 여러 권한을 가질 수 있음
     *	단방향(유저 -> 권한 방향으로만 조회가 가능)이라 AuthUsersEntity 에서만 설정
	 * 	User 엔티티가 auth_users_roles 테이블을 직접 조작할 수 있도록 설정한 것
     * 
     *	@JoinTable(name = "auth_users_roles")
	 *		- User ↔ Role의 연결을 담당할 중간 테이블을 지정
	 *	joinColumns = @JoinColumn(name = "user_id")
	 *		- auth_users_roles 테이블에서 user_id가 auth_users 테이블의 User_ID를 참조함
	 *	inverseJoinColumns = @JoinColumn(name = "role_id")
	 *		- auth_users_roles 테이블에서 role_id가 auth_roles 테이블의 Role_ID를 참조함
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "auth_users_roles",
        joinColumns = @JoinColumn(name = "user_id"),  // auth_users_roles.user_id
        inverseJoinColumns = @JoinColumn(name = "role_id") // auth_users_roles.role_id
    )
    @Builder.Default // roles 필드를 초기화하여 null이 되지 않도록 함, 처음 회원가입때는 권한 안넣어줌
    private Set<AuthRolesEntity> roles = new HashSet<>();
}
