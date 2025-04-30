package com.sim.board.controller;

import com.sim.board.domain.board;
import com.sim.board.domain.comment;
import com.sim.board.domain.fileupload;
import com.sim.board.domain.user;
import com.sim.board.service.auth_service;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class board_controller { // 게시판 요청 처리 [CRUD,file_upload,comment]

    private final board_service boardService;
    private final comment_service commentService;
    private final file_upload_service fileService;
    private final user_service userService;
    private final auth_service authService;

    // 게시글 목록 조회 [GET]
    @GetMapping
    public String list(Model model,
                       @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) { // 페이징 기본값 ID 내림 차순 정렬
        //페이징 처리된 게시글 목록 조회 (page 조회)
        Page<board> boards = boardService.getBoardList(pageable);
        model.addAttribute("boards", boards); //boards 데이터 추가시킴
        return "board/list"; // list.html 반환
    }

    // 게시글 작성 페이지 [GET]
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new board()); //새로운 board 전달 (빈 게시글)
        return "board/write"; //write.html 반환
    }

    // 게시글 저장 [POST]
    @PostMapping
    public String write(@ModelAttribute board board, // 데이터 바인딩
                        @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles, // 첨부 파일 저장 [선택]
                        Authentication authentication) throws IOException { //현재 로그인된 사용자 저장

        // 인증 정보에서 사용자명 추출
        String username = authService.extractUsername(authentication);
        // 사용자 정보 조회
        user user = userService.getUserByUsername(username);
        board savedBoard = boardService.createBoard(board, user); //게시글 생성

        //사용자가 파일을 업로드 했을때
        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            for (MultipartFile file : uploadFiles) {
                if (!file.isEmpty()) {
                    fileService.uploadFile(file, savedBoard.getId()); //파일을 저장 , DB에 기록시킴
                }
            }
        }

        return "redirect:/boards";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            System.out.println("게시글 상세 조회 - 시작 (ID: " + id + ")");

            // 게시글 상세 데이터 조회
            board board = boardService.getBoard(id);
            List<comment> comments = commentService.getCommentsByBoardId(id);
            List<fileupload> files = fileService.getFilesByBoardId(id);

            // 현재 로그인한 사용자 정보 조회
            user currentUser = null;
            boolean isAdmin = false;

            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getName().equals("anonymousUser")) {
                String username = authService.extractUsername(authentication);
                if (username != null) {
                    try {
                        currentUser = userService.getUserByUsername(username);
                        isAdmin = currentUser.getRole().equals(user.ROLE_ADMIN);
                    } catch (Exception e) {
                        System.err.println("현재 사용자 정보 조회 실패: " + e.getMessage());
                    }
                }
            }

            // 모델에 데이터 추가
            model.addAttribute("board", board);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new comment());
            model.addAttribute("files", files);
            model.addAttribute("currentUser", currentUser); // 현재 사용자 정보
            model.addAttribute("isAdmin", isAdmin); // 관리자 여부

            System.out.println("게시글 상세 조회 - 성공");
            return "board/detail";
        } catch (Exception e) {
            System.err.println("게시글 상세 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "게시글을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/boards";
        }
    }
    // 2. editForm 메서드 수정 - OAuth2 사용자 처리 추가
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        board board = boardService.getBoard(id);

        // 인증 정보에서 사용자명 추출
        String username = authService.extractUsername(authentication);
        user user = userService.getUserByUsername(username);

        // 작성자 본인 또는 관리자만 수정할 수 있음
        boolean isAuthor = board.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(com.sim.board.domain.user.ROLE_ADMIN);

        if (!isAuthor && !isAdmin) {
            return "redirect:/boards/" + id;
        }

        List<fileupload> files = fileService.getFilesByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("files", files);
        return "board/edit";
    }

    // 3. update 메서드 수정 - OAuth2 사용자 처리 추가
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute board board,
                         @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                         @RequestParam(name = "deleteExistingFiles", required = false) Boolean deleteExistingFiles,
                         Authentication authentication) {

        // 인증 정보에서 사용자명 추출
        String username = authService.extractUsername(authentication);
        user user = userService.getUserByUsername(username);

        try {
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
        } catch (Exception e) {
            System.err.println("게시글 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/boards";
        }
    }

    // 게시글 삭제 처리 [GET]
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) { //URL 에서 id 추출 , 현재 로그인한 사용자
        // 인증 정보에서 사용자명 추출
        String username = authService.extractUsername(authentication);
        user user = userService.getUserByUsername(username);
        boardService.deleteBoard(id, user); //게시글 삭제 board_service

        return "redirect:/boards";
    }

    // 파일 다운로드 [GET]
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        fileupload file = fileService.getFile(fileId); //파일 정보 조회

        //파일 경로로 Resource 객체 생성
        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        //파일이 존재 유무 판단 , 읽을 수 있는 파일인지 확인
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다: " + file.getOriginalFilename());
        }

        // 파일명에 한글이 포함된 경우 인코딩 처리
        String encodedFilename = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // 파일 다운로드를 위한 http 응답 설정
        // Content-Type을 application/octet-stream으로 설정하여 무조건 다운로드 되도록 설정
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) //바이너리 데이터 타입
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource); //파일 내용
    }
}