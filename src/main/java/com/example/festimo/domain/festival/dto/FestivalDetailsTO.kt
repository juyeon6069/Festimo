package com.example.festimo.domain.festival.dto

data class FestivalDetailsTO(
    val details: List<Detail> = emptyList()
) {
    data class Detail(
        val infoName: String,
        val infoText: String,
        val contentId: Int
    )
}
