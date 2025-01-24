package com.example.festimo.domain.post.entity;

public enum PostCategory {
    ETC("기타"),
    COMPANION("동행자 모집"),
    REVIEW("후기"),
    QNA("Q&A");

    private final String displayName;

    PostCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PostCategory fromDisplayName(String displayName) {
        for (PostCategory category : PostCategory.values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + displayName);
    }
}