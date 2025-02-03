package com.example.festimo.domain.meet.dto

interface ApplicateUsersProjection {
    fun getUserId(): Long
    fun getNickname(): String
    fun getGender(): String
    fun getRatingAvg(): Double
}