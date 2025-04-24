package com.sim.board.repository;

import com.sim.board.domain.fileupload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface file_upload_repository extends JpaRepository<fileupload, Long> {
    List<fileupload> findByBoardId(Long boardId);  // 게시글 ID로 파일 목록 조회
}