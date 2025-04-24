package com.sim.board.controller;

import com.sim.board.domain.board;
import com.sim.board.domain.comment;
import com.sim.board.domain.fileupload;
import com.sim.board.domain.user;
import com.sim.board.service.board_service;
import com.sim.board.service.comment_service;
import com.sim.board.service.file_upload_service;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class board_controller {

    private final board_service boardService;
    private final comment_service commentService;
    private final file_upload_service fileService;
    private final user_service userService;

    // 게시글 목록
    @GetMapping
    public String list(Model model,
                       @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword) {

        Page<board> boards;

        if (keyword != null && !keyword.isEmpty()) {
            if ("title".equals(searchType)) {
                boards = boardService.searchBoardsByTitle(keyword, pageable);
            } else if ("content".equals(searchType)) {
                boards = boardService.searchBoardsByContent(keyword, pageable);
            } else {
                boards = boardService.getBoardList(pageable);
            }
        } else {
            boards = boardService.getBoardList(pageable);
        }

        model.addAttribute("boards", boards);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "board/list";
    }

    // 게시글 작성 페이지
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new board());
        return "board/write";
    }

    // 게시글 저장
    @PostMapping
    public String write(@ModelAttribute board board,
                        @RequestParam(required = false) List<MultipartFile> files,
                        @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        user user = userService.getUserByUsername(userDetails.getUsername());
        board savedBoard = boardService.createBoard(board, user);

        // 파일 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    fileService.uploadFile(file, savedBoard.getId());
                }
            }
        }

        return "redirect:/boards";
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        board board = boardService.getBoard(id);
        List<comment> comments = commentService.getCommentsByBoardId(id);
        List<fileupload> files = fileService.getFilesByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new comment());
        model.addAttribute("files", files);

        return "board/detail";
    }

    // 게시글 수정 페이지
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        board board = boardService.getBoard(id);
        user user = userService.getUserByUsername(userDetails.getUsername());

        // 작성자 확인
        if (!board.getUser().getId().equals(user.getId())) {
            return "redirect:/boards/" + id;
        }

        model.addAttribute("board", board);
        return "board/edit";
    }

    // 게시글 수정
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute board board,
                         @AuthenticationPrincipal UserDetails userDetails) {

        user user = userService.getUserByUsername(userDetails.getUsername());
        boardService.updateBoard(id, board, user);

        return "redirect:/boards/" + id;
    }

    // 게시글 삭제
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        user user = userService.getUserByUsername(userDetails.getUsername());
        boardService.deleteBoard(id, user);

        return "redirect:/boards";
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        fileupload file = fileService.getFile(fileId);

        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                .body(resource);
    }
}