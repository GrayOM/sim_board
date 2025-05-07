// src/main/java/com/sim/board/controller/api_board_controller.java 파일 수정

package com.sim.board.controller;

import com.sim.board.domain.board;
import com.sim.board.service.board_service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@Tag(name = "게시글 API", description = "게시글 CRUD 기능 API")
public class api_board_controller {

    private final board_service boardService;

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이징 처리된 게시글 목록을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "게시글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = Page.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<board>> getBoards(
            @Parameter(description = "페이지 정보 (페이지 번호, 크기, 정렬)")
            @PageableDefault() Pageable pageable) {
        Page<board> boards = boardService.getBoardList(pageable);
        return ResponseEntity.ok(boards);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 특정 게시글을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "게시글 조회 성공",
                            content = @Content(schema = @Schema(implementation = board.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글이 존재하지 않음"
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<board> getBoard(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id) {
        board board = boardService.getBoard(id);
        return ResponseEntity.ok(board);
    }

    @Operation(
            summary = "게시글 생성",
            description = "새로운 게시글을 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "게시글 생성 성공",
                            content = @Content(schema = @Schema(implementation = board.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<board> createBoard(
            @Parameter(description = "게시글 생성 정보", required = true)
            @RequestBody board board) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.ok(board);
    }

    @Operation(
            summary = "게시글 수정",
            description = "기존 게시글을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "게시글 수정 성공",
                            content = @Content(schema = @Schema(implementation = board.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글이 존재하지 않음"
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<board> updateBoard(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "수정할 게시글 정보", required = true)
            @RequestBody board board) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.ok(board);
    }

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "게시글 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글이 존재하지 않음"
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id) {
        // 실제 구현은 없지만 Swagger 문서화를 위해 메서드 추가
        return ResponseEntity.noContent().build();
    }
}