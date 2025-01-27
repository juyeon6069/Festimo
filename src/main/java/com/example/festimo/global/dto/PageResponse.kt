package com.example.festimo.global.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(private val page: Page<T>) {
    val content: List<T> = page.content
    val currentPage: Int = page.number + 1
    val size: Int = page.size
    val totalElements: Long = page.totalElements
    val totalPages: Int = page.totalPages
    val hasNext: Boolean = page.hasNext()
    val hasPrevious: Boolean = page.hasPrevious()
}