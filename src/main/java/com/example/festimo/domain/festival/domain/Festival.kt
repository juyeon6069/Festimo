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
    val festivalId: Long = 0,
    val title: String = "",
    val category: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val address: String = "",
    val image: String? = null,
    val xCoordinate: Float? = null,
    val yCoordinate: Float? = null,
    val phone: String? = null,
    val contentId: Int = 0
)
