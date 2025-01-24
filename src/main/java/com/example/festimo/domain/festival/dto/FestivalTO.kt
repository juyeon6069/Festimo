package com.example.festimo.domain.festival.dto

import java.time.LocalDate

data class FestivalTO(
    var festival_id: Long = 0,
    var title: String = "",
    var category: String? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var address: String = "",
    var image: String? = null,
    var xCoordinate: Float? = null,
    var yCoordinate: Float? = null,
    var phone: String? = null,
    var contentId: Int = 0,
    var festivalDetails: FestivalDetailsTO? = null
)
