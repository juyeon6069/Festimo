package com.example.festimo.domain.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.exception.CustomException;

import static com.example.festimo.exception.ErrorCode.POST_NOT_FOUND;

@Service
public class AdminPostService {

    private final PostRepository postRepository;

    public AdminPostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * 관리자가 게시글을 삭제합니다.
     *
     * @param postId 삭제할 게시글의 ID
     * @throws CustomException 게시글이 존재하지 않을 경우 POST_NOT_FOUND 예외 발생
     */
    @Transactional
    public void deletePostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
        postRepository.delete(post);
    }
}
