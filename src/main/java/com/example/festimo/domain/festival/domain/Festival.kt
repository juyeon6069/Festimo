package com.example.festimo.domain.festival.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
data class Festival(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    var contentId: Int = 0
)
