package com.sim.board.domain;

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
@Table(name = "users")  // 'user' mysql 명령어 이므로 table name 은 users 로 설정
public class user {

    // 사용자 역할 상수 정의
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Id //primary key 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키 자동 생성 , 1씩 증가
    private Long id; // 사용자 고유 번호

    @Column(nullable = false, unique = true) //not null 중복 허용 x
    private String username;  // 사용자 로그인 ID

    @Column(nullable = false) //not null
    private String password;  // 사용자 비밀번호

    @Column(nullable = false) //not null
    private String name;  // 사용자 이름

    @Column(nullable = false, unique = true) // not null , 중복 x
    private String email;  // 사용자 이메일

    private String role;  // 사용자의 권한 게시글 수정,삭제 등등
}