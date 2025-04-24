package com.sim.board.repository;

import com.sim.board.domain.board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface board_repository extends JpaRepository<board, Long> {
    Page<board> findByTitleContaining(String title, Pageable pageable);  // 제목으로 검색
    Page<board> findByContentContaining(String content, Pageable pageable);  // 내용으로 검색
}