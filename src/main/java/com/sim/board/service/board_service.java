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

    private final board_repository boardRepository; // 게시글 DB 작업 담당 레포지터리

    // 게시글 목록 조회 (페이징)
    @Transactional(readOnly = true)  //읽기 전용
    public Page<board> getBoardList(Pageable pageable) {
        // 페이징 정보에 따라 게시글 목록 조회 후 반환
        return boardRepository.findAll(pageable);
    }

    // 게시글 상세 조회
    @Transactional(readOnly = true)
    public board getBoard(Long id) {
        // ID로 게시글 조회, 없으면 예외 발생 시킴
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    // 게시글 작성
    @Transactional
    public board createBoard(board board, user user) {
        board.setUser(user); //게시글에 작성자 정보 설정
        return boardRepository.save(board); //DB에 저장 후 엔티티 반환
    }

    // 게시글 수정
    @Transactional
    public void updateBoard(Long id, board boardRequest, user user) {
        board board = getBoard(id); // 기존에 있는 게시글 조회

        // 작성자 확인 또는 관리자 권한 확인 (인가 검사)
        if (!board.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("게시글 수정 권한이 없습니다.");
        }

        //제목과 내용 업데이트
        board.setTitle(boardRequest.getTitle());
        board.setContent(boardRequest.getContent());

        boardRepository.save(board); //변경 내용 저장
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long id, user user) {
        board board = getBoard(id); //게시글 조회

        // 작성자 확인 또는 관리자 권한 확인 (인가 검사)
        if (!board.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("게시글 삭제 권한이 없습니다.");
        }

        boardRepository.delete(board); // 모든 내용 삭제 (파일,댓글 등등)
    }
}