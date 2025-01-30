package com.example.festimo.domain.admin.controller

import jakarta.validation.Valid

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

import com.example.festimo.domain.admin.dto.AdminUpdateUserDTO
import com.example.festimo.domain.admin.service.AdminService

@RestController
@RequestMapping("/api/admin")
@Tag(name = "관리자 API", description = "관리자가 회원 정보를 관리하는 API")
class AdminController(private val adminService: AdminService) {


    /**
     * 모든 회원 조회
     * @param page 조회할 페이지
     * @param size 페이지 사이즈
     */
    @GetMapping("/users")
    @Operation(summary = "관리자의 회원 조회", description = "모든 회원 정보")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) = ResponseEntity.ok(adminService.getAllUsers(page, size))

    /**
     * 회원 정보 수정
     * @param userId 수정할 회원의 ID
     * @param adminUpdateUserDTO 수정할 회원 정보
     * @return 수정된 회원의 정보
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "관리자의 회원 수정", description = "회원 정보 수정")
    fun updateUser(
        @PathVariable userId: Long,
        @Valid @RequestBody adminUpdateUserDTO: AdminUpdateUserDTO
    ) = ResponseEntity.ok(adminService.updateUser(userId, adminUpdateUserDTO))

    /**
     * 회원 삭제
     * @param userId 삭제할 회원의 ID
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "관리자의 회원 삭제", description = "회원 정보 삭제")
    fun deleteUser(@PathVariable userId: Long) =
        adminService.deleteUser(userId).let { ResponseEntity.noContent().build<Void>() }
}


