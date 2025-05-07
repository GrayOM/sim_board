package com.sim.board.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Schema(description = "사용자 엔티티")
public class user {

    // 사용자 역할 상수 정의
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "사용자 로그인 ID", example = "user1")
    private String username;

    @Column(nullable = false)
    @Schema(description = "사용자 비밀번호", example = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Column(nullable = false, unique = true)
    @Schema(description = "사용자 이메일", example = "user1@example.com")
    private String email;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String role;

    @Schema(description = "소셜 로그인 제공자", example = "google")
    private String provider;

    @Schema(description = "소셜 로그인 제공자 ID")
    private String providerId;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;
}