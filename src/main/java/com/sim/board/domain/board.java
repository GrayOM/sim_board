package com.sim.board.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class board {

    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키 자동 생성 , 1씩 증가
    private Long id; // 게시글 고유 번호 id

    @Column(nullable = false) //NOT NULL
    private String title;  // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT") //게시글 내용 TEXT 타입으로 저으이
    private String content;  // 게시글 내용

    @ManyToOne(fetch = FetchType.LAZY) //  n:1 (n 게시글 -> 1 사용자)
    @JoinColumn(name = "user_id") // user_id 외래키 설정
    private user user;  // 작성자

    @CreationTimestamp // 게시글 생성 시 현재 시간 설정
    private LocalDateTime createdAt;  // 생성 시간

    @UpdateTimestamp // 수정 시 현재 시간 설정
    private LocalDateTime updatedAt;  // 수정 시간

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isModified = false;  // 수정 여부 표시

    // (1 게시글 -> n 댓글) , 게시글 삭제 시 댓글 함께 삭제
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<comment> comments = new ArrayList<>();  // 댓글 목록

    // (1 게시글 -> n 파일목록) , 게시글 삭제 시 파일 목록 삭제
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<fileupload> files = new ArrayList<>();  // 첨부 파일 목록
}