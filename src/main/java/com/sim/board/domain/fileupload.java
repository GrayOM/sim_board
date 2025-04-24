package com.sim.board.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "upload_files")  // table name
public class fileupload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;  // 파일 원본 이름

    @Column(nullable = false)
    private String storedFilename;  // 서버 기준으로 저장된 파일 이름

    @Column(nullable = false)
    private String filePath;  // 서버 기준 파일 저장 위치

    private Long fileSize;  // 파일 크기 [byte]

    @ManyToOne(fetch = FetchType.LAZY) //업로드 시킨 파일은 -> 게시글에 속함
    @JoinColumn(name = "board_id") // board_id 외래키 설정
    private board board;  // 파일이 업로드 된 게시글

    @CreationTimestamp
    private LocalDateTime uploadDate;  // 업로드 일자
}