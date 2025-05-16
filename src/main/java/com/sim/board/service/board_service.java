// board_service.java 수정
package com.sim.board.service;

import com.sim.board.domain.board;
import com.sim.board.domain.user;
import com.sim.board.repository.board_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class board_service {

    private final board_repository boardRepository;
    private final HtmlSanitizerService sanitizerService;

    // 게시글 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<board> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public board getBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    // 게시글 작성
    @Transactional
    public board createBoard(board board, user user) {
        // XSS 방지를 위한 HTML 살균
        board.setTitle(sanitizerService.sanitizeStrict(board.getTitle()));
        board.setContent(sanitizerService.sanitizeContent(board.getContent()));

        board.setUser(user);
        return boardRepository.save(board);
    }

    // 게시글 수정
    @Transactional
    public void updateBoard(Long id, board boardRequest, user user) {
        board board = getBoard(id);

        // 작성자 확인 또는 관리자 권한 확인 (인가 검사)
        if (!board.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("게시글 수정 권한이 없습니다.");
        }

        // XSS 방지를 위한 HTML 살균
        board.setTitle(sanitizerService.sanitizeStrict(boardRequest.getTitle()));
        board.setContent(sanitizerService.sanitizeContent(boardRequest.getContent()));

        board.setIsModified(true);
        board.setUpdatedAt(LocalDateTime.now());

        boardRepository.save(board);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id, user user) {
        board board = getBoard(id);

        // 작성자 확인 또는 관리자 권한 확인 (인가 검사)
        if (!board.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("게시글 삭제 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }
}