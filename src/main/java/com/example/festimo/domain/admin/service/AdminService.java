package com.example.festimo.domain.admin.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.festimo.domain.admin.dto.AdminDTO;
import com.example.festimo.domain.admin.dto.AdminUpdateUserDTO;
import com.example.festimo.domain.admin.mapper.AdminMapper;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.CustomException;
import com.example.festimo.exception.ErrorCode;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 모든 회원을 페이지네이션하여 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 회원 목록
     */
    @Transactional(readOnly = true)
    public Page<AdminDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(AdminMapper.INSTANCE::toDto);
    }

    /**
     * 회원 정보를 수정합니다.
     *
     * @param userId 수정할 회원의 ID
     * @param dto 수정할 회원 정보
     * @return 수정된 회원 정보
     * @throws CustomException 회원이 존재하지 않을 경우 USER_NOT_FOUND 예외 발생
     */
    @Transactional
    public AdminDTO updateUser(Long userId, AdminUpdateUserDTO dto) {
        User user = getUserById(userId);
        AdminMapper.INSTANCE.updateFromDto(dto, user);
        User updatedUser = userRepository.save(user);
        return AdminMapper.INSTANCE.toDto(updatedUser);
    }

    /**
     * 회원을 삭제합니다.
     *
     * @param userId 삭제할 회원의 ID
     * @throws CustomException 회원이 존재하지 않을 경우 USER_NOT_FOUND 예외 발생
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}