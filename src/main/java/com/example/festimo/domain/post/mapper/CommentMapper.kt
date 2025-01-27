package com.example.festimo.domain.post.mapper

import com.example.festimo.domain.post.dto.CommentResponse
import com.example.festimo.domain.post.entity.Comment
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface CommentMapper {
    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "owner", constant = "false")
    @Mapping(target = "admin", constant = "false")
    fun toCommentResponse(comment: Comment): CommentResponse
}