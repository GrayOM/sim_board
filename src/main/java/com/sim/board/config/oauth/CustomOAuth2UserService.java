package com.sim.board.config.oauth;

import com.sim.board.config.oauth.userinfo.OAuth2UserInfo;
import com.sim.board.config.oauth.userinfo.OAuth2UserInfoFactory;
import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final user_repository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // 이메일이 없는 경우 예외 처리
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider: " + registrationId);
        }

        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        // 디버깅용 로그
        System.out.println("OAuth2 Provider: " + registrationId);
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("ID: " + oAuth2UserInfo.getId());

        // 이메일로 기존 사용자가 있는지 확인
        Optional<user> userOptional = userRepository.findByEmail(email);
        user userEntity;

        if (userOptional.isPresent()) {
            // 기존 사용자가 있다면 소셜 정보 업데이트
            userEntity = userOptional.get();

            // 소셜 로그인 정보가 다르다면 업데이트
            if (!userEntity.getProvider().equals(oAuth2UserInfo.getProvider()) ||
                    !userEntity.getProviderId().equals(oAuth2UserInfo.getId())) {
                userEntity.setProvider(oAuth2UserInfo.getProvider());
                userEntity.setProviderId(oAuth2UserInfo.getId());
                userEntity.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
                userEntity.setName(oAuth2UserInfo.getName());
                userRepository.save(userEntity); // 값을 할당하지 않고 직접 메소드만 호출
            }
        } else {
            // 새 사용자라면 저장
            userEntity = user.builder()
                    .username(email) // 이메일을 사용자명으로 사용
                    .email(email)
                    .name(name)
                    .password("oauth2user") // 소셜 로그인 사용자는 실제 사용하지 않는 비밀번호
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getId())
                    .profileImageUrl(oAuth2UserInfo.getImageUrl())
                    .role(user.ROLE_USER) // 기본 역할은 USER
                    .build();

            userEntity = userRepository.save(userEntity); // 새 사용자의 경우 저장된 엔티티가 필요함
        }

        // OAuth2User 생성 및 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole())),
                attributes,
                oAuth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}