package com.sim.board.repository;

import com.sim.board.domain.board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface board_repository extends JpaRepository<board, Long> {
    Page<board> findAll(Pageable pageable);  // 페이징 처리
    Page<board> findByTitleContaining(String title, Pageable pageable);  // 제목으로 검색
    Page<board> findByContentContaining(String content, Pageable pageable);  // 내용으로 검색
    Page<board> findByUser_Username(String username, Pageable pageable);  // 특정 사용자가 작성한 게시글 검색
}