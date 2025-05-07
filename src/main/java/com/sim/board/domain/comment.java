package com.sim.board.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 엔티티")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @Schema(description = "댓글이 달린 게시글")
    @JsonBackReference
    private board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "댓글 작성자")
    private user user;

    @CreationTimestamp
    @Schema(description = "댓글 생성 시간", example = "2025-04-30T10:15:00")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "댓글 수정 시간", example = "2025-04-30T10:20:00")
    private LocalDateTime updatedAt;
}