package com.example.festimo.domain.post.entity

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class PostCategory {
    COMPANION,
    REVIEW,
    QNA,
    ETC;

    val displayName: String
        get() = when (this) {
            COMPANION -> "동행자 모집"
            REVIEW -> "후기"
            QNA -> "Q&A"
            ETC -> "기타"
        }

    companion object {
        @JvmStatic
        fun fromDisplayName(displayName: String): PostCategory =
            values().find { it.displayName == displayName }
                ?: throw IllegalArgumentException("존재하지 않는 카테고리 입니다: $displayName")
    }
}