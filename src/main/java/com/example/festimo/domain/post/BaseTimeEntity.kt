package com.example.festimo.domain.post

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    var updatedAt: LocalDateTime? = null
        protected set

    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now()
        }
        updatedAt = null
    }
}