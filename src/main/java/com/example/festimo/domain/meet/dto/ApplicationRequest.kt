package com.example.festimo.domain.meet.dto

data class ApplicationRequest(
    val companionId: Long
)
{
    // 기본 생성자 필요
    constructor() : this(0)
}