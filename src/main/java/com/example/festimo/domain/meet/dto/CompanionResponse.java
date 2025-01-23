package com.example.festimo.domain.meet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CompanionResponse {

    private Long companionId;  //모임 id
    private Long leaderId;  //리더 id
    private String leaderName;  //리더 이름
    private List<MemberResponse> members;  //동행원들

    @Data
    @AllArgsConstructor
    public static class MemberResponse {

        private Long userId;
        private String userName;
    }


}