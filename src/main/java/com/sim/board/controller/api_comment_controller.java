package com.sim.board.controller;

import com.sim.board.domain.comment;
import com.sim.board.service.comment_service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "게시글의 댓글 목록 조회", description = "게시글 ID로 해당 게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<comment>> getCommentsByBoardId(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long boardId) {
        List<comment> comments = commentService.getCommentsByBoardId(boardId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 작성, 수정, 삭제 API 메서드 추가
}