package com.example.festimo.domain.post.dto;

import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.entity.PostCategory;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long id;
    private String nickname;
    private String avatar;
    private String time;
    private String title;
    private String content;
    private List<String> tags;
    private int replies;
    private int views;
    private PostCategory category;

    public PostListResponse(Post post) {
        this.id = post.getId();
        this.nickname = post.getUser().getNickname();
        this.avatar = (post.getUser().getAvatar() != null && !post.getUser().getAvatar().isEmpty())
                ? "/imgs/" + post.getUser().getAvatar()
                : "/imgs/default-avatar.png";
        this.time = post.getCreatedAt() != null ? calculateTime(post.getCreatedAt()) : "Unknown time";
        this.title = post.getTitle();
        this.content = post.getContent();
        this.tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();
        this.replies = (post.getComments() != null)
                ? post.getComments().size()
                : 0;
        this.views = post.getViews();
        this.category = post.getCategory();
    }

    private String calculateTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "Unknown time";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        if (duration.toMinutes() < 60) {
            long minutes = duration.toMinutes();
            return minutes + (minutes == 1 ? " min ago" : " mins ago");
        } else if (duration.toHours() < 24) {
            long hours = duration.toHours();
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (duration.toDays() <= 7) {
            long days = duration.toDays();
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return createdAt.format(formatter);
        }
    }
}