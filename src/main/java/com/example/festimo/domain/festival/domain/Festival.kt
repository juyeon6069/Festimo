package com.example.festimo.domain.festival.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "festival",
    indexes = [
        Index(name = "idx_festival_date", columnList = "start_date, end_date")
    ]
)
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
