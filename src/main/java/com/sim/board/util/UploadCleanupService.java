// src/main/java/com/sim/board/util/UploadCleanupService.java
package com.sim.board.util;

import com.sim.board.domain.fileupload;
import com.sim.board.repository.file_upload_repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UploadCleanupService {

    private static final Logger logger = Logger.getLogger(UploadCleanupService.class.getName());

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final file_upload_repository fileRepository;

    public UploadCleanupService(file_upload_repository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOrphanedFiles() {
        logger.info("업로드 폴더 정리 작업 시작");

        try {
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists() || !uploadDirectory.isDirectory()) {
                logger.warning("업로드 디렉토리가 존재하지 않습니다: " + uploadDir);
                return;
            }

            // DB에 등록된 모든 파일명 목록 가져오기
            List<fileupload> allFiles = fileRepository.findAll();
            Set<String> registeredFilenames = allFiles.stream()
                    .map(fileupload::getStoredFilename)
                    .collect(Collectors.toSet());

            // 업로드 디렉토리의 모든 파일 검사
            File[] files = uploadDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 파일이 DB에 등록되지 않은 경우 (고아 파일)
                    if (!registeredFilenames.contains(file.getName())) {
                        // 파일 생성 후 24시간 이상 지난 경우에만 삭제
                        LocalDateTime creationTime = LocalDateTime.ofInstant(
                                Files.getLastModifiedTime(file.toPath()).toInstant(),
                                ZoneId.systemDefault());

                        if (creationTime.plusDays(1).isBefore(LocalDateTime.now())) {
                            boolean deleted = file.delete();
                            if (deleted) {
                                logger.info("미등록 파일 삭제됨: " + file.getName());
                            } else {
                                logger.warning("미등록 파일 삭제 실패: " + file.getName());
                            }
                        }
                    }
                }
            }

            logger.info("업로드 폴더 정리 작업 완료");
        } catch (Exception e) {
            logger.severe("업로드 폴더 정리 중 오류 발생: " + e.getMessage());
        }
    }

    // 위험 파일 확장자 즉시 삭제 (애플리케이션 시작 시 실행)
    public void removeUnsafeFiles() {
        logger.info("위험 파일 검사 시작");

        // 위험한 파일 확장자 목록
        Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
                "php", "jsp", "js", "html", "htm", "exe", "sh", "bat", "cmd", "com", "jar", "war",
                "ear", "class", "asp", "aspx", "cgi", "pl", "py", "rb", "php5", "phtml", "shtml"
        ));

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath) || !Files.isDirectory(uploadPath)) {
                return;
            }

            Files.list(uploadPath).forEach(path -> {
                String filename = path.getFileName().toString().toLowerCase();
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0) {
                    String extension = filename.substring(dotIndex + 1);
                    if (DANGEROUS_EXTENSIONS.contains(extension)) {
                        try {
                            Files.delete(path);
                            logger.warning("위험 파일 삭제됨: " + filename);

                            // DB에서 해당 파일 정보도 삭제
                            fileRepository.findAll().stream()
                                    .filter(file -> filename.equals(file.getStoredFilename()))
                                    .forEach(fileRepository::delete);

                        } catch (Exception e) {
                            logger.severe("위험 파일 삭제 중 오류: " + e.getMessage());
                        }
                    }
                }
            });

            logger.info("위험 파일 검사 완료");
        } catch (Exception e) {
            logger.severe("위험 파일 검사 중 오류 발생: " + e.getMessage());
        }
    }
}