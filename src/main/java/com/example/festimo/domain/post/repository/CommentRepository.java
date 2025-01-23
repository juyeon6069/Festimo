package com.example.festimo.domain.post.repository;

import com.example.festimo.domain.post.entity.Comment;
import com.example.festimo.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시글별 댓글의 최대 sequence 조회
    @Query("SELECT COALESCE(MAX(c.sequence), 0) FROM Comment c WHERE c.post = :post")
    Integer findMaxSequenceByPost(@Param("post") Post post);

    // 게시글과 sequence를 기준으로 댓글 찾기
    Optional<Comment> findByPostIdAndSequence(Long postId, Integer sequence);

    List<Comment> findByPostOrderBySequenceAsc(Post post);
}