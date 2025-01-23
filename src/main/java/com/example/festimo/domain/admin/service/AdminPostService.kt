package com.example.festimo.domain.admin.service

import com.example.festimo.domain.post.repository.PostRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminPostService(private val postRepository: PostRepository) {
    /**
     * 관리자가 게시글을 삭제합니다.
     *
     * @param postId 삭제할 게시글의 ID
     * @throws CustomException 게시글이 존재하지 않을 경우 POST_NOT_FOUND 예외 발생
     */
    @Transactional
    fun deletePostById(postId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { CustomException(ErrorCode.POST_NOT_FOUND) }
        postRepository.delete(post)
    }
}
