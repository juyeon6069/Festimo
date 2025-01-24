package com.example.festimo.domain.post.entity;

import com.example.festimo.domain.post.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private String nickname;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 게시글별 댓글 순번
    @Column(nullable = false)
    private Integer sequence;

    public void updateContent(String newComment) {
        this.comment = newComment;
    }
}