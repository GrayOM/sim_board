package com.sim.board.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class comment { //게시판 댓글

    @Id //댓글 id primarykey
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 1씩 증가
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT") // 댓글 내용 Text 타입
    private String content;  // 댓글 내용

    @ManyToOne(fetch = FetchType.LAZY) //댓글 -> 댓글쓴 게시판에 대해서 속함
    @JoinColumn(name = "board_id") //board_id 외래키로 설정
    private board board;  // 게시글

    @ManyToOne(fetch = FetchType.LAZY) // 댓글 -> 댓글쓴 사용자에 속함
    @JoinColumn(name = "user_id") // user_id 외래키로 설정
    private user user;  // 작성자

    @CreationTimestamp
    private LocalDateTime createdAt;  // 댓글 생성시간

    @UpdateTimestamp
    private LocalDateTime updatedAt;  // 댓글을 수정한 시간
}