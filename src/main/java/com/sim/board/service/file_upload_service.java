// src/main/java/com/sim/board/service/file_upload_service.java
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

    // 허용된 파일 확장자 목록
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
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
        String extension = getExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        String filePath = uploadPath + File.separator + storedFilename;

        // 파일 저장
        Path targetLocation = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetLocation);

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
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
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