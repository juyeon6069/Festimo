package com.example.festimo.domain.user.service

import com.example.festimo.domain.review.repository.ReviewRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.dto.*
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import com.example.festimo.global.utils.jwt.JwtTokenProvider
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Supplier


@Service
//@RequiredArgsConstructor
open class UserService(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val modelMapper: ModelMapper
) {
    companion object{
        private val logger:Logger = LoggerFactory.getLogger(UserService::class.java)
    }
    @Transactional
    open fun updateUserRatingAvg(userId: Long?) {
        var averageRating = reviewRepository!!.findAverageRatingByRevieweeId(userId)
        if (averageRating == null) {
            averageRating = 0.0 // 리뷰가 없을 경우 기본값 설정
        }
        userRepository!!.updateRatingAvg(userId, averageRating)
    }

    // 회원가입
    @Transactional
    open fun register(dto: UserRegisterRequestDTO): String {
        val email = normalizeEmail(dto.email)
        logger.info("Attempting to register user with email: {}", email)

        if (userRepository!!.existsByEmail(email)) {
            logger.warn("Registration failed. Email already exists: {}", email)
            throw CustomException(ErrorCode.DUPLICATE_EMAIL)
        }

        if (userRepository.existsByNickname(dto.nickname)) {
            logger.warn("Registration failed. Nickname already exists: {}", dto.nickname)
            throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
        }

        validatePassword(dto.password)

        // DTO → Entity 변환
        val user = modelMapper!!.map(
            dto,
            User::class.java
        )
        user.email= email // 소문자로 변환한 이메일 저장
        user.password = passwordEncoder!!.encode(dto.password)
        user.role = User.Role.USER
        user.provider = User.Provider.LOCAL
        user.ratingAvg = 0.0f // 기본값 설정

        // Gender 처리
        try {
            user.gender = dto.gender?.let { User.Gender.valueOf(it.uppercase()) }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid gender value provided: {}", dto.gender)
            throw CustomException(ErrorCode.INVALID_GENDER)
        }


        userRepository.save(user)
        logger.info("User registered successfully with email: {}", email)
        return "User registered successfully."
    }

    fun login(dto: UserLoginRequestDTO): TokenResponseDTO {
        println("login Service")
        val email = normalizeEmail(dto.email)
        logger.info("Attempting login for email: {}", email)
        logger.info("UserRepository : {}", userRepository.toString())
        val user = userRepository!!.findByEmail(email)
            ?.orElseThrow(Supplier {
                logger.warn("Login failed. User not found for email: {}", email)
                BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다..")
            })!!

        if (user.password != null && !passwordEncoder!!.matches(dto.password, user.password)) {
            logger.warn("Login failed. Invalid credentials for email: {}", email)
            throw CustomException(ErrorCode.INVALID_CREDENTIALS)
        }

        val tokens = regenerateTokens(user)
        user.refreshToken = tokens.refreshToken
        println("리프레쉬 토큰 : " + tokens.refreshToken)
        userRepository.save(user)
        println(user)

        tokens.nickname = user.nickname
        tokens.email = user.email

        logger.info("Login successful for email: {}", email)
        return tokens
    }

    @Transactional
    open fun logout(refreshToken: String?) {
        logger.info("Attempting logout for refresh token.")
        val user = userRepository!!.findByRefreshToken(refreshToken)
            ?.orElseThrow(Supplier {
                logger.warn("Logout failed. Invalid refresh token.")
                CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
            })!!

        user.refreshToken = null
        userRepository.save(user)

        logger.info("Logout successful.")
    }

    @Transactional
    open fun changePassword(email: String?, dto: ChangePasswordDTO): String {
        // 이메일로 사용자 조회
        val user = userRepository!!.findByEmail(normalizeEmail(email))
            ?.orElseThrow(Supplier { CustomException(ErrorCode.USER_NOT_FOUND) })!!

        // 기존 비밀번호 검증
        if (!passwordEncoder!!.matches(dto.oldPassword, user.password)) {
            throw CustomException(ErrorCode.INVALID_OLD_PASSWORD)
        }

        // 새 비밀번호 유효성 검사
        validatePassword(dto.newPassword)

        // 비밀번호 변경 및 저장
        user.password = passwordEncoder.encode(dto.newPassword)
        userRepository.save(user)

        return "Password changed successfully."
    }

    // review에 쓰일 아이디 추출
    fun getUserIdByEmail(email: String?): Long? {
        return userRepository!!.findByEmail(email)
            ?.map{ it?.id } // User 엔티티에서 ID 추출
            ?.orElseThrow<CustomException>(Supplier<CustomException> { CustomException(ErrorCode.USER_NOT_FOUND) })
    }


    // 사용자 정보 반환, 성별 넣을지 말지
    fun getUserByEmail(email: String?): UserResponseDTO {
        val user = userRepository!!.findByEmail(normalizeEmail(email))
            ?.orElseThrow(Supplier { CustomException(ErrorCode.USER_NOT_FOUND) })!!

        val responseDTO = modelMapper!!.map(user, UserResponseDTO::class.java)

        // Gender를 String으로 변환하여 설정
        responseDTO.gender = if (user.gender != null) user.gender!!.name else null

        return responseDTO
    }

    // 회원 정보 수정
    @Transactional
    open fun updateUser(email: String?, dto: UserUpdateRequestDTO): String {
        val user = userRepository!!.findByEmail(normalizeEmail(email))
            ?.orElseThrow(Supplier { CustomException(ErrorCode.USER_NOT_FOUND) })!!

        // 닉네임 업데이트
        if (dto.nickname != null) {
            // 자신이 사용 중인 닉네임인지 확인
            if (!user.nickname.equals(dto.nickname) && userRepository.existsByNickname(dto.nickname)) {
                throw CustomException(ErrorCode.DUPLICATE_NICKNAME)
            }
            user.nickname= dto.nickname
        }


        // 사용자 이름 업데이트
        if (dto.nickname != null) {
            user.nickname = dto.nickname
        }

        // 성별 업데이트
        if (dto.gender != null) {
            try {
                val gender = User.Gender.valueOf(dto.gender.uppercase())
                user.gender = gender
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_GENDER)
            }
        }

        // 변경 사항 저장
        userRepository.save(user)

        return "User updated successfully."
    }


    // 회원 삭제
    @Transactional
    open fun deleteUser(email: String?): String {
        // 이메일로 회원 조회
        val user = userRepository!!.findByEmail(email)
            ?.orElseThrow(Supplier { CustomException(ErrorCode.USER_NOT_FOUND) })!!

        // 회원 삭제
        userRepository.delete(user)

        return "User deleted successfully."
    }

    // 이메일 소문자로 변환
    private fun normalizeEmail(email: String?): String? {
        return email?.lowercase(Locale.getDefault())
    }

    // 비밀번호 검증
    private fun validatePassword(password: String) {
        // 비밀번호가 null인 경우 예외 발생
        if (password == null) {
            throw CustomException(ErrorCode.PASSWORD_CANNOT_BE_NULL)
        }

        // 비밀번호 길이 검사
        if (password.length < 8 || password.length > 20) {
            throw CustomException(ErrorCode.PASSWORD_INVALID_LENGTH)
        }

        // 비밀번호에 문자 포함 여부 검사
        if (!password.matches(".*[A-Za-z].*".toRegex())) {
            throw CustomException(ErrorCode.PASSWORD_MISSING_LETTER)
        }

        // 비밀번호에 숫자 포함 여부 검사
        if (!password.matches(".*\\d.*".toRegex())) {
            throw CustomException(ErrorCode.PASSWORD_MISSING_NUMBER)
        }

        // 비밀번호에 특수문자 포함 여부 검사
        if (!password.matches(".*[@$!%*?&].*".toRegex())) {
            throw CustomException(ErrorCode.PASSWORD_MISSING_SPECIAL_CHARACTER)
        }
    }


    // Refresh Token을 사용해 Access Token을 재발급
    @Transactional
    open fun refreshTokens(refreshToken: String): TokenResponseDTO {
        logger.info("Attempting to refresh tokens.")

        // Refresh Token 유효성 검사
        if (!jwtTokenProvider!!.validateToken(refreshToken)) {
            logger.warn("Token refresh failed. Invalid refresh token.")
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val email = jwtTokenProvider.getEmailFromToken(refreshToken)

        // 이메일로 사용자 조회
        val user = userRepository!!.findByEmail(email)
            ?.orElseThrow(Supplier {
                logger.warn("Token refresh failed. User not found for email: {}", email)
                CustomException(ErrorCode.USER_NOT_FOUND)
            })!!

        // Refresh Token 매칭 여부 확인
        if (refreshToken != user.refreshToken) {
            logger.warn("Token refresh failed. Refresh token mismatch for email: {}", email)
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 토큰 재발급
        val tokens = regenerateTokens(user)
        logger.info("Tokens refreshed successfully for email: {}", email)
        return tokens
    }

    private fun regenerateTokens(user: User): TokenResponseDTO {
        val newAccessToken = jwtTokenProvider!!.generateAccessToken(user.email, user.role?.name ?: "USER")
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user.email)

        user.refreshToken = newRefreshToken
        userRepository!!.save(user)

        return TokenResponseDTO(
            newAccessToken,
            newRefreshToken,
            user.nickname,
            user.email
        )
    }
}
