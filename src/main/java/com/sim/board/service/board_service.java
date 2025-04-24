package com.sim.board.service;

import com.sim.board.domain.board;
import com.sim.board.domain.user;
import com.sim.board.repository.board_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class board_service {

    private final board_repository boardRepository;

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
        board.setUser(user);
        return boardRepository.save(board);
    }

    // 게시글 수정
    @Transactional
    public board updateBoard(Long id, board boardRequest, user user) {
        board board = getBoard(id);

        // 작성자 확인
        if (!board.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("게시글 수정 권한이 없습니다.");
        }

        board.setTitle(boardRequest.getTitle());
        board.setContent(boardRequest.getContent());

        return boardRepository.save(board);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id, user user) {
        board board = getBoard(id);

        // 작성자 확인
        if (!board.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("게시글 삭제 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }

    // 제목으로 게시글 검색
    @Transactional(readOnly = true)
    public Page<board> searchBoardsByTitle(String title, Pageable pageable) {
        return boardRepository.findByTitleContaining(title, pageable);
    }

    // 내용으로 게시글 검색
    @Transactional(readOnly = true)
    public Page<board> searchBoardsByContent(String content, Pageable pageable) {
        return boardRepository.findByContentContaining(content, pageable);
    }
}