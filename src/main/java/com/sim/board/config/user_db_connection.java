package com.sim.board.config;

import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

//Spring security 가 사용자 인증을 수행할때 사용자 정보를 DB에서 조회하기 위한 클래스.
@Service
@RequiredArgsConstructor //생성자 자동 주입 (user)
public class user_db_connection implements UserDetailsService {

    private final user_repository userRepository; //사용자 정보 조회

    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션을 통해서 사용자가 데이터베이스에 있는지 조회
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //DB 에서 username 기준으로 사용자 정보를 조회함
        user user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        // 조회가 된다면 UserDetails 객체 변환 시킴
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // ID
                user.getPassword(), // PW
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}