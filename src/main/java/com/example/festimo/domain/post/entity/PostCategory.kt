package com.example.festimo.domain.post.entity

enum class PostCategory(val displayName: String) {
    ETC("기타"),
    COMPANION("동행자 모집"),
    REVIEW("후기"),
    QNA("Q&A");

    companion object {
        @JvmStatic
        fun fromDisplayName(displayName: String): PostCategory =
            values().find { it.displayName == displayName }
                ?: throw IllegalArgumentException("존재하지 않는 카테고리 입니다: $displayName")
    }
}