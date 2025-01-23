package com.example.festimo.domain.festival.dto

import java.time.LocalDate

data class FestivalTO(
    val festival_id: Long = 0,
    val title: String,
    val category: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val address: String,
    val image: String?,
    val xCoordinate: Float?,
    val yCoordinate: Float?,
    val phone: String?,
    val contentId: Int,
    var festivalDetails: FestivalDetailsTO?
)
