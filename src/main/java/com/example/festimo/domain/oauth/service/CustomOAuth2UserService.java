package com.example.festimo.domain.oauth.service;

import com.example.festimo.domain.oauth.dto.CustomOAuth2User;
import com.example.festimo.domain.oauth.dto.KakaoOAuth2Response;
import com.example.festimo.domain.oauth.dto.NaverOAuth2Response;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("Token Request: " + userRequest.getAccessToken());
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("Kakao Response: " + oAuth2User.getAttributes()); // 디버깅용

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("naver".equals(registrationId)) {
            return processNaverUser(oAuth2User);
        } else if ("kakao".equals(registrationId)) {
            return processKakaoUser(oAuth2User);
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 제공자입니다.");
        }
    }

    private OAuth2User processNaverUser(OAuth2User oAuth2User) {
        NaverOAuth2Response naverResponse = new NaverOAuth2Response(oAuth2User.getAttributes());
        String email = naverResponse.getEmail();

        User user = findOrCreateUser(
                email,
                naverResponse.getName(),
                naverResponse.getGender(),
                User.Provider.NAVER,
                naverResponse.getProviderId()
        );

        return mapToOauth2User(user);
    }

    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 전체 속성 데이터
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account"); // kakao_account 추출

        // kakao_account 데이터 로그 출력
        System.out.println("Full Kakao attributes: " + attributes);
        System.out.println("Kakao account details: " + kakaoAccount);

        KakaoOAuth2Response kakaoResponse = new KakaoOAuth2Response(oAuth2User.getAttributes());

        User user = findOrCreateUser(
            kakaoResponse.getEmail(),
            kakaoResponse.getNickname(),
            null, // 카카오는 성별 제공 안 함
            User.Provider.KAKAO,
            kakaoResponse.getProviderId()
        );

        return mapToOauth2User(user);
    }

    private User findOrCreateUser(String email, String name, String gender, User.Provider provider, String providerId) {
        User user = userRepository.findByProviderId(providerId) // 수정됨
                .orElse(null); // Optional 처리
        if (user == null) {
            return createNewUser(email, name, gender, provider, providerId);
        } else {
            updateUser(user, provider, providerId, email);
            return user;
        }
    }

    private User createNewUser(String email, String name, String gender, User.Provider provider, String providerId) { // 사용자 생성 로직 분리
        User user = new User();
        user.setUserName(email);
        user.setEmail(email);
        user.setNickname(name);
        user.setRole(User.Role.USER);
        if ("M".equals(gender)) {
            user.setGender(User.Gender.M);
        } else if ("F".equals(gender)) {
            user.setGender(User.Gender.F);
        }
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setRefreshToken(jwtTokenProvider.generateRefreshToken(email));
        userRepository.save(user);
        return user;
    }

    private void updateUser(User user, User.Provider provider, String providerId, String email) {
        // 사용자 업데이트 로직 분리 필요 없는것 빼기
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmail(email);
        user.setRefreshToken(jwtTokenProvider.generateRefreshToken(email));
        userRepository.save(user);
    }

    private OAuth2User mapToOauth2User(User user) { // 수정됨
        return new CustomOAuth2User(
                Map.of(), // 속성 데이터 (원본 데이터는 필요 시 확장 가능)
                user.getProvider().name(), // 제공자 이름
                user.getEmail(), // 이메일
                user.getNickname(), // 닉네임
                user.getRole().name() // 역할
        );
    }


}
