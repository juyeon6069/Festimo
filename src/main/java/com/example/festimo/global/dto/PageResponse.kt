package com.example.festimo.global.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int currentPage;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber() + 1;
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
}