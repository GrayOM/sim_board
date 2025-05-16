package com.sim.board.service;

import com.sim.board.domain.board;
import com.sim.board.domain.fileupload;
import com.sim.board.repository.board_repository;
import com.sim.board.repository.file_upload_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class file_upload_service {

    private final file_upload_repository fileRepository;
    private final board_repository boardRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 허용된 파일 확장자 목록 - 위험한 확장자 제외
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    ));

    // 차단할 파일 확장자 목록 - 실행 가능한 위험한 파일
    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "php", "jsp", "js", "html", "htm", "exe", "sh", "bat", "cmd", "com", "jar", "war",
            "ear", "class", "asp", "aspx", "cer", "cgi", "cfm", "dll", "jspx", "py", "pl", "rb"
    ));

    // 위험한 MIME 타입 목록
    private static final Set<String> BLOCKED_MIME_TYPES = new HashSet<>(Arrays.asList(
            "application/x-msdownload", "application/x-msdos-program", "application/x-javascript",
            "application/javascript", "application/x-php", "application/x-jsp", "text/javascript",
            "application/octet-stream", "application/x-httpd-php"
    ));

    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Transactional
    public fileupload uploadFile(MultipartFile file, Long boardId) throws IOException {
        // 게시글 존재 여부 확인
        board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 파일 검증
        validateFile(file);

        // 파일 저장 경로 생성
        String uploadPath = new File(uploadDir).getAbsolutePath();
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if(!created) {
                throw new IOException("디렉토리 생성에 실패했습니다 : " + uploadPath);
            }
        }

        // 저장할 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        String filePath = uploadPath + File.separator + storedFilename;

        // 파일 저장
        Path targetLocation = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetLocation);

        // 추가 보안 검사: 파일 컨텐츠 검사
        if (isExecutableContent(targetLocation)) {
            Files.delete(targetLocation); // 위험한 파일 삭제
            throw new IllegalArgumentException("위험한 파일 컨텐츠가 감지되었습니다.");
        }

        // 파일 정보 데이터베이스에 저장
        fileupload fileEntity = fileupload.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath)
                .fileSize(file.getSize())
                .board(board)
                .build();

        return fileRepository.save(fileEntity);
    }

    // 파일 검증 메소드
    private void validateFile(MultipartFile file) {
        // 빈 파일 체크
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 파일 크기 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 5MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 체크
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 파일명입니다.");
        }

        String extension = getExtension(originalFilename).toLowerCase();

        // 금지된 확장자 확인
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("업로드가 금지된 파일 형식입니다: " + extension);
        }

        // 허용된 확장자가 아닌 경우
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용된 형식: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // MIME 타입 확인
        String contentType = file.getContentType();
        if (contentType != null && BLOCKED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("업로드가 금지된 파일 유형입니다: " + contentType);
        }
    }

    // 실행 가능한 컨텐츠 체크 (간단한 시그니처 검사)
    private boolean isExecutableContent(Path filePath) {
        try {
            byte[] content = Files.readAllBytes(filePath);

            // 실행 파일 시그니처 (예: MZ 헤더)
            if (content.length >= 2 && content[0] == 'M' && content[1] == 'Z') {
                return true;
            }

            // 스크립트 패턴 검사
            String contentStr = new String(content).toLowerCase();

            // 위험한 스크립트 패턴 검사
            return contentStr.contains("<?php") ||
                    contentStr.contains("<script") ||
                    contentStr.contains("<%@") ||
                    contentStr.contains("<%=") ||
                    contentStr.contains("eval(") ||
                    contentStr.contains("system(") ||
                    contentStr.contains("exec(") ||
                    contentStr.contains("runtime.getruntime") ||
                    contentStr.contains("processbuilder");
        } catch (IOException e) {
            // 파일 읽기 실패 시 안전을 위해 true 반환
            return true;
        }
    }

    // 파일 확장자 추출 메소드
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    // 나머지 메서드는 그대로 유지
    @Transactional(readOnly = true)
    public fileupload getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<fileupload> getFilesByBoardId(Long boardId) {
        return fileRepository.findByBoardId(boardId);
    }

    @Transactional
    public void deleteFile(Long fileId) {
        fileupload file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }

        fileRepository.delete(file);
    }
}