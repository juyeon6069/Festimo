package com.example.festimo.domain.post.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
data class Tag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String = "",
    var count: Int = 0,

    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null
)