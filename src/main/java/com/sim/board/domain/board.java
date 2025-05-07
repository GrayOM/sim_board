package com.sim.board.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "게시글 엔티티")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "게시글 제목", example = "안녕하세요, 첫 게시글입니다.")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 반갑습니다.")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "게시글 작성자")
    private user user;

    @CreationTimestamp
    @Schema(description = "게시글 생성 시간", example = "2025-04-30T10:00:00")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "게시글 수정 시간", example = "2025-04-30T10:30:00")
    private LocalDateTime updatedAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    @Schema(description = "게시글 수정 여부", example = "false")
    private Boolean isModified = false;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "게시글에 달린 댓글 목록")
    @JsonManagedReference
    private List<comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "게시글에 첨부된 파일 목록")
    @JsonManagedReference
    private List<fileupload> files = new ArrayList<>();
}