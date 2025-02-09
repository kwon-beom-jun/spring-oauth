package com.example.oauth.repository;

import com.example.oauth.entity.AuthRolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRolesRepository extends JpaRepository<AuthRolesEntity, Integer> {
	
}
