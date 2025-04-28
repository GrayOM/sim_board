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
public class board_controller { // 게시판 요청 처리 [CRUD,file_upload,comment]

    private final board_service boardService;
    private final comment_service commentService;
    private final file_upload_service fileService;
    private final user_service userService;

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
                        @AuthenticationPrincipal UserDetails userDetails) throws IOException { //현재 로그인된 사용자 저장

        user user = userService.getUserByUsername(userDetails.getUsername()); //현재 로그인 된 사용자 정보를 조회함
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

    // 게시글 상세 조회 [GET /boards/{id}]
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        board board = boardService.getBoard(id); //URL에서 추출한 ID로 게시글 조회
        List<comment> comments = commentService.getCommentsByBoardId(id); //선택한 게시글의 댓글 목록 조회
        List<fileupload> files = fileService.getFilesByBoardId(id); //선택한 게시글의 첨부 파일 목록 조회

        // 모델에 데이터 추가
        model.addAttribute("board", board);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new comment()); //새로운 댓글
        model.addAttribute("files", files);

        return "board/detail"; //detail.html 반환
    }

    // 게시글 수정 페이지 [GET]
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        board board = boardService.getBoard(id); //URL 에서 추출한 ID로 게시글 조회함
        user user = userService.getUserByUsername(userDetails.getUsername()); // 현재 로그인한 사용자 정보 조회

        // 작성자 본인만 수정할 수 있음
        if (!board.getUser().getId().equals(user.getId())) {
            return "redirect:/boards/" + id; //만약 작성자 본인이 아니라면 상세페이지로 이동
        }

        // 파일 목록 가져오기
        List<fileupload> files = fileService.getFilesByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("files", files);  // 파일 목록을 모델에 추가
        return "board/edit"; // 수정 페이지 반환
    }

    // 게시글 수정
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, //URL 경로에서 게시글 ID 추출
                         @ModelAttribute board board, // 수정할 게시글 데이터 폼에서 매핑
                         @RequestParam(name = "uploadFiles", required = false) List<MultipartFile> uploadFiles, //새로 업로드할 파일 목록 [선택]
                         @RequestParam(name = "deleteExistingFiles", required = false) Boolean deleteExistingFiles, //기존 파일을 삭제할지 여부 [선택]
                         @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        user user = userService.getUserByUsername(userDetails.getUsername()); //현재 로그인한 사용자 정보를 조회
        boardService.updateBoard(id, board, user); // 게시글 수정

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
                    fileService.uploadFile(file, id); //파일을 저장하고 DB 에 기록시킴
                }
            }
        }

        return "redirect:/boards/" + id;
    }

    // 게시글 삭제 처리 [GET]
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) { //URL 에서 id 추출 , 현재 로그인한 사용자
        user user = userService.getUserByUsername(userDetails.getUsername()); //현재 로그인한 사용자 정보 조회
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