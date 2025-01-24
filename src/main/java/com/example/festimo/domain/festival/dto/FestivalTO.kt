package com.example.festimo.domain.festival.dto

import java.time.LocalDate

data class FestivalTO(
    val festival_id: Long = 0,
    val title: String = "",
    val category: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val address: String = "",
    val image: String? = null,
    val xCoordinate: Float? = null,
    val yCoordinate: Float? = null,
    val phone: String? = null,
    val contentId: Int = 0,
    var festivalDetails: FestivalDetailsTO? = null
)
