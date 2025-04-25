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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                       @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<board> boards = boardService.getBoardList(pageable);
        model.addAttribute("boards", boards);

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
                        @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                        @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        user user = userService.getUserByUsername(userDetails.getUsername());
        board savedBoard = boardService.createBoard(board, user);

        // 파일 처리
        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            for (MultipartFile file : uploadFiles) {
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

        // 파일 목록 조회
        List<fileupload> files = fileService.getFilesByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("files", files);  // 파일 목록을 모델에 추가
        return "board/edit";
    }

    // 게시글 수정
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute board board,
                         @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                         @RequestParam(name = "deleteExistingFiles", required = false) Boolean deleteExistingFiles,
                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        user user = userService.getUserByUsername(userDetails.getUsername());
        boardService.updateBoard(id, board, user);

        // 기존 파일 삭제 옵션이 선택된 경우
        if (deleteExistingFiles != null && deleteExistingFiles) {
            // 기존 파일 목록 조회
            List<fileupload> existingFiles = fileService.getFilesByBoardId(id);
            // 기존 파일 삭제
            for (fileupload file : existingFiles) {
                fileService.deleteFile(file.getId());
            }
        }

        // 새 파일 업로드 처리
        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            for (MultipartFile file : uploadFiles) {
                if (!file.isEmpty()) {
                    fileService.uploadFile(file, id);
                }
            }
        }

        return "redirect:/boards/" + id;
    }

    // 게시글 삭제
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        user user = userService.getUserByUsername(userDetails.getUsername());
        boardService.deleteBoard(id, user);

        return "redirect:/boards";
    }

    // 파일 다운로드
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        fileupload file = fileService.getFile(fileId);

        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다: " + file.getOriginalFilename());
        }

        // 파일명에 한글이 포함된 경우 인코딩 처리
        String encodedFilename = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // Content-Type을 application/octet-stream으로 설정하여 무조건 다운로드 되도록 함
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }
}