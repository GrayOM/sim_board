package com.sim.board.repository;

import com.sim.board.domain.board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface board_repository extends JpaRepository<board, Long> {

}