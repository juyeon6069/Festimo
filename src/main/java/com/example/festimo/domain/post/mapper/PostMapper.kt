package com.example.festimo.domain.post.mapper

import com.example.festimo.domain.post.dto.PostDetailResponse
import com.example.festimo.domain.post.entity.Post
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring", uses = [CommentMapper::class])
interface PostMapper {
    @Mapping(target = "postId", source = "id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "owner", constant = "false")
    @Mapping(target = "admin", constant = "false")
    @Mapping(target = "liked", constant = "false")
    @Mapping(target = "comments", source = "comments")
    fun postToPostDetailResponse(post: Post): PostDetailResponse
}