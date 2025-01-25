package com.example.festimo.domain.meet.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.example.festimo.exception.ErrorCode.USER_NOT_FOUND;

import com.example.festimo.domain.meet.dto.CompanionRequest;
import com.example.festimo.domain.meet.dto.CompanionResponse;
import com.example.festimo.domain.meet.service.CompanionService;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.CustomException;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;


@RestController
@RequestMapping("/api/meet")
@Tag(name = "동행 API", description = "동행 관련 API")
public class CompanionController {

    private final CompanionService companionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public CompanionController(CompanionService companionService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.companionService = companionService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    private String getEmailFromHeader(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtTokenProvider.getEmailFromToken(token);
    }

    private User getUserFromEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    @PostMapping("/companions")
    @Operation(summary = "동행 생성")
    public ResponseEntity<Void> createCompanion(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CompanionRequest request) {

        String email = getEmailFromHeader(authorizationHeader);
        companionService.createCompanion(request.getPostId(), email);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{companionId}")
    @Operation(summary = "동행 취소")
    public ResponseEntity<Void> deleteCompanion(
            @PathVariable Long companionId,
            @RequestHeader("Authorization") String authorizationHeader) {

        String email = getEmailFromHeader(authorizationHeader);
        companionService.deleteCompanion(companionId, email);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/companions/mine")
    @Operation(summary = "내 동행 찾기")
    public ResponseEntity<Map<String, Object>> getMyCompanions(
            @RequestHeader("Authorization") String authorizationHeader) {

        String email = getEmailFromHeader(authorizationHeader);
        User user = getUserFromEmail(email);

        List<CompanionResponse> asLeader = companionService.getCompanionAsLeader(user.getId());
        List<CompanionResponse> asMember = companionService.getCompanionAsMember(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("asLeader", asLeader);
        response.put("asMember", asMember);

        return ResponseEntity.ok(response);
    }
}