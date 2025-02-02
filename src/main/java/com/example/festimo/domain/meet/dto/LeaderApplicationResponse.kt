package com.example.festimo.domain.meet.dto

data class LeaderApplicationResponse(

    val userId: Long,
    val nickname: String,
    val gender: String,
    val ratingAvg: Double

)