package com.example.oauth.repository;

import com.example.oauth.entity.AuthUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 유저 정보를 DB에서 조회하기 위한 JpaRepository
 */
public interface AuthUsersRepository extends JpaRepository<AuthUsersEntity, Integer> {
    Optional<AuthUsersEntity> findByUsername(String username);
}
