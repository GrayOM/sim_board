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

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "upload_files")
@Schema(description = "파일 업로드 엔티티")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class fileupload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "파일 ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "원본 파일명", example = "document.pdf")
    private String originalFilename;

    @Column(nullable = false)
    @Schema(description = "저장된 파일명", example = "uuid_document.pdf")
    private String storedFilename;

    @Column(nullable = false)
    @Schema(description = "파일 저장 경로", example = "/app/uploads/uuid_document.pdf")
    private String filePath;

    @Schema(description = "파일 크기(바이트)", example = "1024")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    @Schema(description = "파일이 첨부된 게시글")
    @JsonBackReference
    private board board;

    @CreationTimestamp
    @Schema(description = "파일 업로드 일자", example = "2025-04-30T10:05:00")
    private LocalDateTime uploadDate;
}