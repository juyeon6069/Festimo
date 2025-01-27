package com.example.festimo.domain.user.service;


import com.example.festimo.domain.review.repository.ReviewRepository;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.dto.*;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.CustomException;
import com.example.festimo.exception.ErrorCode;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public void updateUserRatingAvg(Long userId) {
        Double averageRating = reviewRepository.findAverageRatingByRevieweeId(userId);
        if (averageRating == null) {
            averageRating = 0.0; // 리뷰가 없을 경우 기본값 설정
        }
        userRepository.updateRatingAvg(userId, averageRating);
    }

    // 회원가입
    @Transactional
    public String register(UserRegisterRequestDTO dto) {

        String email = normalizeEmail(dto.getEmail());
        logger.info("Attempting to register user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed. Email already exists: {}", email);
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(dto.getNickname())) {
            logger.warn("Registration failed. Nickname already exists: {}", dto.getNickname());
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        validatePassword(dto.getPassword());

        // DTO → Entity 변환
        User user = modelMapper.map(dto, User.class);
        user.setEmail(email);  // 소문자로 변환한 이메일 저장
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setProvider(User.Provider.LOCAL);
        user.setRatingAvg(0.0f); // 기본값 설정

        // Gender 처리
        try {
            user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid gender value provided: {}", dto.getGender());
            throw new CustomException(ErrorCode.INVALID_GENDER);
        }


        userRepository.save(user);
        logger.info("User registered successfully with email: {}", email);
        return "User registered successfully.";
    }

    public TokenResponseDTO login(UserLoginRequestDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        logger.info("Attempting login for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed. User not found for email: {}", email);
                    return new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다..");
                });

        if (user.getPassword()!=null &&!passwordEncoder.matches(dto.getPassword(), user.getPassword()) ) {
            logger.warn("Login failed. Invalid credentials for email: {}", email);
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        TokenResponseDTO tokens = regenerateTokens(user);
        user.setRefreshToken(tokens.getRefreshToken());
        System.out.println("리프레쉬 토큰 : " + tokens.getRefreshToken());
        userRepository.save(user);
        System.out.println(user);

        tokens.setNickname(user.getNickname());
        tokens.setEmail(user.getEmail());

        logger.info("Login successful for email: {}", email);
        return tokens;
    }

    @Transactional
    public void logout(String refreshToken) {
        logger.info("Attempting logout for refresh token.");
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    logger.warn("Logout failed. Invalid refresh token.");
                    return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        user.setRefreshToken(null);
        userRepository.save(user);

        logger.info("Logout successful.");
    }

    @Transactional
    public String changePassword(String email, ChangePasswordDTO dto) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        // 새 비밀번호 유효성 검사
        validatePassword(dto.getNewPassword());

        // 비밀번호 변경 및 저장
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully.";
    }

    // review에 쓰일 아이디 추출
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(User::getId) // User 엔티티에서 ID 추출
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }



    // 사용자 정보 반환, 성별 넣을지 말지
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() ->  new CustomException(ErrorCode.USER_NOT_FOUND));

        UserResponseDTO responseDTO = modelMapper.map(user, UserResponseDTO.class);

        // Gender를 String으로 변환하여 설정
        responseDTO.setGender(user.getGender() != null ? user.getGender().name() : null);

        return responseDTO;
    }

    // 회원 정보 수정
    @Transactional
    public String updateUser(String email, UserUpdateRequestDTO dto) {
        User user = userRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() ->  new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 업데이트
        if (dto.getNickname() != null) {
            // 자신이 사용 중인 닉네임인지 확인
            if (!user.getNickname().equals(dto.getNickname()) && userRepository.existsByNickname(dto.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.setNickname(dto.getNickname());
        }


        // 사용자 이름 업데이트
        if (dto.getUserName() != null) {
            user.setUserName(dto.getUserName());
        }

        // 성별 업데이트
        if (dto.getGender() != null) {
            try {
                User.Gender gender = User.Gender.valueOf(dto.getGender().toUpperCase());
                user.setGender(gender);
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_GENDER);
            }
        }

        // 변경 사항 저장
        userRepository.save(user);

        return "User updated successfully.";
    }



    // 회원 삭제
    @Transactional
    public String deleteUser(String email) {
        // 이메일로 회원 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 회원 삭제
        userRepository.delete(user);

        return "User deleted successfully.";
    }

    // 이메일 소문자로 변환
    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase() : null;
    }

    // 비밀번호 검증
    private void validatePassword(String password) {
        // 비밀번호가 null인 경우 예외 발생
        if (password == null) {
            throw new CustomException(ErrorCode.PASSWORD_CANNOT_BE_NULL);
        }

        // 비밀번호 길이 검사
        if (password.length() < 8 || password.length() > 20) {
            throw new CustomException(ErrorCode.PASSWORD_INVALID_LENGTH);
        }

        // 비밀번호에 문자 포함 여부 검사
        if (!password.matches(".*[A-Za-z].*")) {
            throw new CustomException(ErrorCode.PASSWORD_MISSING_LETTER);
        }

        // 비밀번호에 숫자 포함 여부 검사
        if (!password.matches(".*\\d.*")) {
            throw new CustomException(ErrorCode.PASSWORD_MISSING_NUMBER);
        }

        // 비밀번호에 특수문자 포함 여부 검사
        if (!password.matches(".*[@$!%*?&].*")) {
            throw new CustomException(ErrorCode.PASSWORD_MISSING_SPECIAL_CHARACTER);
        }
    }



    // Refresh Token을 사용해 Access Token을 재발급
    @Transactional
    public TokenResponseDTO refreshTokens(String refreshToken) {
        logger.info("Attempting to refresh tokens.");

        // Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            logger.warn("Token refresh failed. Invalid refresh token.");
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.warn("Token refresh failed. User not found for email: {}", email);
                return new CustomException(ErrorCode.USER_NOT_FOUND);
            });

        // Refresh Token 매칭 여부 확인
        if (!refreshToken.equals(user.getRefreshToken())) {
            logger.warn("Token refresh failed. Refresh token mismatch for email: {}", email);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 토큰 재발급
        TokenResponseDTO tokens = regenerateTokens(user);
        logger.info("Tokens refreshed successfully for email: {}", email);
        return tokens;
    }

    private TokenResponseDTO regenerateTokens(User user) {
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new TokenResponseDTO(
                newAccessToken,
                newRefreshToken,
                user.getNickname(),
                user.getEmail()
        );
    }

}
