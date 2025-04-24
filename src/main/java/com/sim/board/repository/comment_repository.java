package com.sim.board.repository;

import com.sim.board.domain.comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface comment_repository extends JpaRepository<comment, Long> {
    List<comment> findByBoardId(Long boardId);  // 게시글 ID로 댓글 목록 조회
}