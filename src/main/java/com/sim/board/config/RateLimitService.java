// src/main/java/com/sim/board/config/RateLimitService.java
package com.sim.board.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    // 각 사용자당 1분에 최대 요청 수
    private final int MAX_REQUESTS_PER_MINUTE = 10;
    // 각 사용자당 1시간에 최대 게시글 수
    private final int MAX_POSTS_PER_HOUR = 20;
    // 각 사용자당 1시간에 최대 파일 업로드 수
    private final int MAX_FILE_UPLOADS_PER_HOUR = 50;

    private LoadingCache<String, Integer> requestCounts;
    private LoadingCache<String, Integer> postCounts;
    private LoadingCache<String, Integer> fileUploadCounts;

    public RateLimitService() {
        // 요청 카운트 캐시 - 1분마다 초기화
        requestCounts = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });

        // 게시글 카운트 캐시 - 1시간마다 초기화
        postCounts = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });

        // 파일 업로드 카운트 캐시 - 1시간마다 초기화
        fileUploadCounts = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    // 일반 요청에 대한 레이트 리밋 체크
    public boolean isRequestLimited(String key) {
        try {
            int count = requestCounts.get(key);
            if (count >= MAX_REQUESTS_PER_MINUTE) {
                return true;
            }
            requestCounts.put(key, count + 1);
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    // 게시글 작성에 대한 레이트 리밋 체크
    public boolean isPostLimited(String key) {
        try {
            int count = postCounts.get(key);
            if (count >= MAX_POSTS_PER_HOUR) {
                return true;
            }
            postCounts.put(key, count + 1);
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    // 파일 업로드에 대한 레이트 리밋 체크
    public boolean isFileUploadLimited(String key) {
        try {
            int count = fileUploadCounts.get(key);
            if (count >= MAX_FILE_UPLOADS_PER_HOUR) {
                return true;
            }
            fileUploadCounts.put(key, count + 1);
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }
}