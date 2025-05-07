// src/main/java/com/sim/board/controller/api_comment_controller.java 파일 수정

package com.sim.board.controller;

import com.sim.board.domain.comment;
import com.sim.board.service.comment_service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@Tag(name = "댓글 API", description = "댓글 CRUD 기능 API")
public class api_comment_controller {

    private final comment_service commentService;

    @Operation(
            summary = "게시글의 댓글 목록 조회",
            description = "게시글 ID로 해당 게시글의 댓글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = comment.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글이 존재하지 않음"
                    )
            }
    )
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<comment>> getCommentsByBoardId(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long boardId) {
        List<comment> comments = commentService.getCommentsByBoardId(boardId);
        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "댓글 생성",
            description = "새로운 댓글을 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "댓글 생성 성공",
                            content = @Content(schema = @Schema(implementation = comment.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글이 존재하지 않음"
                    )
            }
    )
    @PostMapping("/board/{boardId}")
    public ResponseEntity<comment> createComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long boardId,
            @Parameter(description = "댓글 내용", required = true)
            @RequestBody comment comment) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "댓글 수정",
            description = "기존 댓글을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 수정 성공",
                            content = @Content(schema = @Schema(implementation = comment.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "댓글이 존재하지 않음"
                    )
            }
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<comment> updateComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "수정할 댓글 내용", required = true)
            @RequestBody comment comment) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.ok(comment);
    }

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "댓글 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "댓글이 존재하지 않음"
                    )
            }
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.noContent().build();
    }
}