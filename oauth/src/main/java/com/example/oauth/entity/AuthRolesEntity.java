package com.example.oauth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_roles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRolesEntity {

    @Id
    @Column(name = "Role_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(name = "Role_Name")
    private String roleName;  // e.g. "ROLE_HRM_MANAGER"

    @Column(name = "Service_Name")
    private String serviceName; // e.g. "hrm", "receipt"
    
}
