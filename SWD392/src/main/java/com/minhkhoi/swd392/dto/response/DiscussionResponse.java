package com.minhkhoi.swd392.dto.response;

import com.minhkhoi.swd392.entity.Discussion;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DiscussionResponse {
    private UUID id;
    private String user;
    private String avatar;
    private String avatarColor;
    private String course;
    private String lesson;
    private String content;
    private String timestamp;
    private String type;
    private String status;
    private Integer likes;
    private String adminReply;

    public static DiscussionResponse fromEntity(Discussion discussion) {
        String initials = "";
        String color = "bg-blue-100 text-blue-600";
        if (discussion.getUser() != null && discussion.getUser().getFullName() != null) {
            String[] parts = discussion.getUser().getFullName().trim().split("\\s+");
            if (parts.length >= 2) {
                initials = parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
            } else if (parts.length == 1 && !parts[0].isEmpty()) {
                initials = parts[0].substring(0, Math.min(2, parts[0].length()));
            }
            initials = initials.toUpperCase();
        } else {
            initials = "U";
        }

        return DiscussionResponse.builder()
                .id(discussion.getDiscussionId())
                .user(discussion.getUser() != null ? discussion.getUser().getFullName() : "Anonymous")
                .avatar(initials)
                .avatarColor(color)
                .course(discussion.getLesson() != null && discussion.getLesson().getModule() != null && discussion.getLesson().getModule().getCourse() != null
                        ? discussion.getLesson().getModule().getCourse().getTitle() : "N/A")
                .lesson(discussion.getLesson() != null ? discussion.getLesson().getTitle() : "N/A")
                .content(discussion.getContent())
                .timestamp(discussion.getCreatedAt() != null ? discussion.getCreatedAt().toString() : "")
                .type(discussion.getType() != null ? discussion.getType().name() : "DISCUSSION")
                .status(discussion.getStatus() != null ? discussion.getStatus().name() : "UNANSWERED")
                .likes(discussion.getLikes() != null ? discussion.getLikes() : 0)
                .adminReply(discussion.getAdminReply())
                .build();
    }
}
