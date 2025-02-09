package com.example.festimo.global.utils.jwt

import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): UserDetails {
        logger.debug("Attempting to load user by username: {}", username)

        val user: User = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username").also {
                logger.error("User not found: {}", username)
            }

        logger.info("User loaded successfully: {}", username)

        return CustomUserDetails(
            email = user.email,
            password = user.password,
            authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }
}
