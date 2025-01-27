package com.example.festimo.domain.meet.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.*


@Embeddable
class CompanionMemberId(
    @Column(name = "companion_id", nullable = false)
    var companionId: Long,

    @Column(name = "user_id", nullable = false)
    var userId: Long
) : Serializable {
    constructor() : this(0, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CompanionMemberId
        return companionId == that.companionId && userId == that.userId
    }

    override fun hashCode(): Int {
        return Objects.hash(companionId, userId)
    }
}