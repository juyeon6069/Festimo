package com.example.festimo.domain.admin.controller

import com.example.festimo.domain.admin.dto.AdminDTO
import com.example.festimo.domain.admin.dto.AdminUpdateUserDTO
import com.example.festimo.domain.admin.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    ): ResponseEntity<Page<AdminDTO>> {
        val users = adminService.getAllUsers(page, size)
        return ResponseEntity.ok(users)
    }

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
        @RequestBody adminUpdateUserDTO: @Valid AdminUpdateUserDTO?
    ): ResponseEntity<AdminDTO> {
        val user = adminService.updateUser(userId, adminUpdateUserDTO)
        return ResponseEntity.ok(user)
    }

    /**
     * 회원 삭제
     * @param userId 삭제할 회원의 ID
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "관리자의 회원 삭제", description = "회원 정보 삭제")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        adminService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}

/*
@RestController
@RequestMapping("/api/admin")
@Tag(name = "관리자 API", description = "관리자가 회원 정보를 관리하는 API")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/users")
    @Operation(summary = "관리자의 회원 조회", description = "모든 회원 정보")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<AdminDTO>> = adminService.getAllUsers(page, size)
        .let(ResponseEntity::ok)

    @PutMapping("/users/{userId}")
    @Operation(summary = "관리자의 회원 수정", description = "회원 정보 수정")
    fun updateUser(
        @PathVariable userId: Long,
        @Valid @RequestBody dto: AdminUpdateUserDTO
    ): ResponseEntity<AdminDTO> = adminService.updateUser(userId, dto)
        .let(ResponseEntity::ok)

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "관리자의 회원 삭제", description = "회원 정보 삭제")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> =
        adminService.deleteUser(userId)
            .let { ResponseEntity.noContent().build() }
}
* */