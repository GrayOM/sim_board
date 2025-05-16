// src/main/java/com/sim/board/config/MaliciousFileCleanupService.java
package com.sim.board.config;

import com.sim.board.domain.fileupload;
import com.sim.board.repository.file_upload_repository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MaliciousFileCleanupService {

    private final file_upload_repository fileRepository;
    private static final Logger logger = Logger.getLogger(MaliciousFileCleanupService.class.getName());

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 위험한 파일 확장자 목록
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
            "php", "jsp", "js", "html", "htm", "exe", "sh", "bat", "cmd", "com", "jar", "war",
            "ear", "class", "asp", "aspx", "cer", "cgi", "cfm", "dll", "jspx", "py", "pl", "rb"
    ));

    // 위험한 파일 패턴
    private static final Set<String> DANGEROUS_PATTERNS = new HashSet<>(Arrays.asList(
            "<?php", "<%@", "<%=", "eval(", "system(", "exec(", "runtime.getruntime",
            "processbuilder", "<script", "shell_exec"
    ));

    @PostConstruct
    @Transactional
    public void cleanupMaliciousFiles() {
        logger.info("시작: 위험한 파일 검사 및 정리...");

        try {
            // 모든 파일 정보 가져오기
            List<fileupload> allFiles = fileRepository.findAll();
            int removedCount = 0;

            for (fileupload file : allFiles) {
                String fileName = file.getOriginalFilename().toLowerCase();
                String extension = getExtension(fileName);

                // 위험한 확장자 검사
                if (DANGEROUS_EXTENSIONS.contains(extension)) {
                    removeMaliciousFile(file);
                    removedCount++;
                    continue;
                }

                // 파일 내용 검사
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath) && isMaliciousContent(filePath)) {
                    removeMaliciousFile(file);
                    removedCount++;
                }
            }

            logger.info("제거된 위험 파일 수: " + removedCount);

        } catch (Exception e) {
            logger.severe("위험 파일 정리 중 오류 발생: " + e.getMessage());
        }
    }

    private boolean isMaliciousContent(Path filePath) {
        try {
            // 텍스트 파일만 검사 (바이너리 파일 제외)
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || !contentType.startsWith("text/")) {
                return false;
            }

            byte[] content = Files.readAllBytes(filePath);
            String contentStr = new String(content).toLowerCase();

            // 위험 패턴 검사
            for (String pattern : DANGEROUS_PATTERNS) {
                if (contentStr.contains(pattern)) {
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            logger.warning("파일 내용 검사 중 오류: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void removeMaliciousFile(fileupload file) {
        try {
            logger.warning("위험한 파일 발견 및 제거: " + file.getOriginalFilename());

            // 실제 파일 삭제
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);

            // DB에서 파일 정보 삭제
            fileRepository.delete(file);

        } catch (Exception e) {
            logger.severe("위험 파일 제거 중 오류: " + e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}