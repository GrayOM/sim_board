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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class file_upload_service {

    private final file_upload_repository fileRepository; //파일 DB 작업 담당
    private final board_repository boardRepository; // 게시글 DB 작업 담당

    @Value("${file.upload-dir}")
    private String uploadDir;  // application.yml에 설정한 업로드 경로

    // 파일 업로드
    @Transactional
    public fileupload uploadFile(MultipartFile file, Long boardId) throws IOException {
        // 게시글 존재 여부 확인
        board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 파일 저장 경로 생성
        String uploadPath = new File(uploadDir).getAbsolutePath();
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            //저장 디렉토리가 없을 경우 디렉토리 생성
            boolean created = dir.mkdirs();
            if(!created) //생성 실패시
            {
                throw new IOException("디렉토리 생성에 실패했습니다 : " + uploadPath);
            }
        }

        // 저장할 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        String filePath = uploadPath + File.separator + storedFilename;

        // 파일 저장
        Path targetLocation = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetLocation);

        // 파일 정보 데이터베이스에 저장
        fileupload fileEntity = fileupload.builder()
                .originalFilename(originalFilename) //원본 파일명
                .storedFilename(storedFilename) // 저장된 파일명
                .filePath(filePath) //파일 경로
                .fileSize(file.getSize()) //파일 크기
                .board(board) //업로드된 게시글
                .build();

        return fileRepository.save(fileEntity); //DB에 저장 후 저장된 엔티티 반환
    }

    // 파일 다운로드를 위한 파일 정보 조회
    @Transactional(readOnly = true)
    public fileupload getFile(Long fileId) {
        // id로 파일 정보 조회 , 없으면 예외
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
    }

    // 게시글에 첨부된 파일 목록 조회
    @Transactional(readOnly = true)
    public List<fileupload> getFilesByBoardId(Long boardId) {
        //게시글 ID로 파일 목록 조회
        return fileRepository.findByBoardId(boardId);
    }

    // 파일 삭제
    @Transactional
    public void deleteFile(Long fileId) {
        fileupload file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

        // 실제 파일 삭제
        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath); //파일이 존재하면 삭제 시킴
        } catch (IOException e) {
            //파일 삭제 실패 시 예외 발생
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }

        // 데이터베이스에서 파일 정보 삭제
        fileRepository.delete(file);
    }
}