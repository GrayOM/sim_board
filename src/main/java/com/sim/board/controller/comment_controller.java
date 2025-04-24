package com.sim.board.controller;

import com.sim.board.domain.comment;
import com.sim.board.domain.user;
import com.sim.board.service.comment_service;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class comment_controller {

    private final comment_service commentService;
    private final user_service userService;

    // 댓글 작성
    @PostMapping("/board/{boardId}")
    public String create(@PathVariable Long boardId,
                         @ModelAttribute comment comment,
                         @AuthenticationPrincipal UserDetails userDetails) {

        user user = userService.getUserByUsername(userDetails.getUsername());
        commentService.createComment(boardId, comment, user);

        return "redirect:/boards/" + boardId;
    }

    // 댓글 수정
    @PostMapping("/{commentId}")
    public String update(@PathVariable Long commentId,
                         @ModelAttribute comment comment,
                         @RequestParam Long boardId,
                         @AuthenticationPrincipal UserDetails userDetails) {

        user user = userService.getUserByUsername(userDetails.getUsername());
        commentService.updateComment(commentId, comment, user);

        return "redirect:/boards/" + boardId;
    }

    // 댓글 삭제
    @GetMapping("/{commentId}/delete")
    public String delete(@PathVariable Long commentId,
                         @RequestParam Long boardId,
                         @AuthenticationPrincipal UserDetails userDetails) {

        user user = userService.getUserByUsername(userDetails.getUsername());
        commentService.deleteComment(commentId, user);

        return "redirect:/boards/" + boardId;
    }
}