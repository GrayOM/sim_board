// src/main/java/com/sim/board/config/oauth/CustomOAuth2UserService.java 파일 개선

package com.sim.board.config.oauth;

import com.sim.board.config.oauth.userinfo.OAuth2UserInfo;
import com.sim.board.config.oauth.userinfo.OAuth2UserInfoFactory;
import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
// Oauth2 로그인 정보 처리
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final user_repository userRepository;

    // PasswordEncoder 의존성 제거하고 직접 BCryptPasswordEncoder 생성
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        try {
            //소셜 로그인 -> 사용자 정보 처리 시킴
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        //소셜 로그인 제공자 ID (google,naver,kakao)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        //사용자 속성 정보 복사
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes()); // 안전한 복사본 생성

        try {
            // OAuth2UserInfo 안전하게 생성
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

            // ID, 이메일, 이름 추출
            String providerId = oAuth2UserInfo.getId();
            String email = oAuth2UserInfo.getEmail();
            String name = oAuth2UserInfo.getName();

            // ID 확인 및 기본값 설정
            if (providerId == null) {
                providerId = "unknown-" + System.currentTimeMillis();
            }

            // 디버깅용 로그
            System.out.println("OAuth2 Provider: " + registrationId);
            System.out.println("Provider ID: " + providerId);
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);

            // 이메일이 없는 경우, 대체 식별자 사용
            user userEntity;
            String username;

            if (!StringUtils.hasText(email)) {
                // 이메일 없이 providerId로 사용자 검색
                username = registrationId + "-" + providerId;
                System.out.println("이메일 없음, 대체 사용자명 사용: " + username);
            } else {
                username = email;
            }

            // 이메일이나 대체 식별자로 사용자 검색
            Optional<user> userOptional;
            if (StringUtils.hasText(email)) {
                userOptional = userRepository.findByEmail(email);
            } else {
                userOptional = userRepository.findByUsername(username);
            }

            if (userOptional.isPresent()) {
                // 기존 사용자가 있다면 업데이트
                userEntity = userOptional.get();

                // 소셜 로그인 정보 업데이트 (필요한 경우)
                userEntity.setProvider(registrationId);
                userEntity.setProviderId(providerId);

                // 이름과 프로필 이미지 URL 업데이트 (있는 경우에만)
                if (StringUtils.hasText(name)) {
                    userEntity.setName(name);
                }

                if (StringUtils.hasText(oAuth2UserInfo.getImageUrl())) {
                    userEntity.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
                }

                // 정해진 역할이 없음 기본 사용자인 ROLE_USER 인가 적용
                if (userEntity.getRole() == null || userEntity.getRole().isEmpty()) {
                    userEntity.setRole(user.ROLE_USER);
                }

                userRepository.save(userEntity);
                System.out.println("기존 사용자 업데이트 완료: " + userEntity.getUsername());
            } else {
                // 새 사용자 생성
                userEntity = user.builder()
                        .username(username)
                        .email(StringUtils.hasText(email) ? email : username)
                        .name(StringUtils.hasText(name) ? name : "사용자-" + providerId)
                        .password(passwordEncoder.encode("oauth2user"))
                        .provider(registrationId)
                        .providerId(providerId)
                        .profileImageUrl(oAuth2UserInfo.getImageUrl())
                        .role(user.ROLE_USER)
                        .build();

                userEntity = userRepository.save(userEntity);
                System.out.println("새 사용자 생성 완료: " + userEntity.getUsername());
            }

            // 사용자 역할 확인 및 디버깅 로그 추가
            System.out.println("User role: " + userEntity.getRole());

            // OAuth2User 생성 및 반환
            String userNameAttributeName = oAuth2UserRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            // userNameAttributeName이 없으면 기본값 설정
            if (!StringUtils.hasText(userNameAttributeName)) {
                userNameAttributeName = "id";
                // 기본 id 속성 추가
                attributes.put("id", providerId);
            }

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole())),
                    attributes,
                    userNameAttributeName
            );
        } catch (Exception e) { //사용자 생성 중 오류 예외 처리
            System.err.println("OAuth2 사용자 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            // 복구 가능한 최소한의 DefaultOAuth2User 객체 생성
            Map<String, Object> recoveryAttributes = new HashMap<>();
            recoveryAttributes.put("id", "recovery-" + System.currentTimeMillis());
            recoveryAttributes.put("name", "복구 사용자");

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.ROLE_USER)),
                    recoveryAttributes,
                    "id"
            );
        }
    }
}