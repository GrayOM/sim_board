package com.sim.board.service;

import com.sim.board.domain.board;
import com.sim.board.domain.comment;
import com.sim.board.domain.user;
import com.sim.board.repository.board_repository;
import com.sim.board.repository.comment_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class comment_service {

    private final comment_repository commentRepository;
    private final board_repository boardRepository;

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }

    // 댓글 작성
    @Transactional
    public void createComment(Long boardId, comment comment, user user) {
        board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        comment.setBoard(board);
        comment.setUser(user);

        commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, comment commentRequest, user user) {
        comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 확인 또는 관리자 권한 확인
        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(commentRequest.getContent());

        commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, user user) {
        comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 확인 또는 관리자 권한 확인
        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}