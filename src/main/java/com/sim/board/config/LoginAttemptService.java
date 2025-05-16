package com.sim.board.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5;
    private final int BLOCK_DURATION_MINUTES = 30;
    private final user_repository userRepository;

    private LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(user_repository userRepository) {
        this.userRepository = userRepository;
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);

        // 사용자 계정이 있으면 마지막 로그인 시간 업데이트
        userRepository.findByUsername(key).ifPresent(user -> {
            user.setLastLoginAttempt(LocalDateTime.now());
            user.setLocked(false); // 계정 잠금 해제
            userRepository.save(user);
        });
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }

        attempts++;
        attemptsCache.put(key, attempts);

        // 최대 시도 횟수를 초과한 경우 사용자 계정 잠금
        if (attempts >= MAX_ATTEMPT) {
            userRepository.findByUsername(key).ifPresent(user -> {
                user.setLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
            });
        }
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }

    // 사용자 계정이 잠겼는지 확인
    public boolean isAccountLocked(String key) {
        return userRepository.findByUsername(key)
                .map(user -> user.getLocked() != null && user.getLocked())
                .orElse(false);
    }

    // 계정 잠금 시간 확인
    public LocalDateTime getAccountLockTime(String key) {
        return userRepository.findByUsername(key)
                .map(user -> user.getLockTime())
                .orElse(null);
    }

    // 잠금 해제까지 남은 시간 (분) 계산
    public long getRemainingLockTime(String key) {
        LocalDateTime lockTime = getAccountLockTime(key);
        if (lockTime == null) {
            return 0;
        }

        LocalDateTime unlockTime = lockTime.plusMinutes(BLOCK_DURATION_MINUTES);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(unlockTime)) {
            // 잠금 시간이 지났으면 계정 잠금 해제
            userRepository.findByUsername(key).ifPresent(user -> {
                user.setLocked(false);
                userRepository.save(user);
            });
            return 0;
        }

        return java.time.Duration.between(now, unlockTime).toMinutes();
    }
}