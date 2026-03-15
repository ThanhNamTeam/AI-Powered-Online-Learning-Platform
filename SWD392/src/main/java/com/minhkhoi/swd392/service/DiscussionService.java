package com.minhkhoi.swd392.service;

import com.minhkhoi.swd392.dto.PageResponse;
import com.minhkhoi.swd392.dto.response.DiscussionResponse;
import com.minhkhoi.swd392.entity.Discussion;
import com.minhkhoi.swd392.entity.User;
import com.minhkhoi.swd392.exception.AppException;
import com.minhkhoi.swd392.exception.ErrorCode;
import com.minhkhoi.swd392.repository.DiscussionRepository;
import com.minhkhoi.swd392.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussionService {

    private final DiscussionRepository discussionRepository;

    public PageResponse<DiscussionResponse> getDiscussions(String filter, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Discussion.DiscussionType type = null;
        Discussion.DiscussionStatus status = null;

        if ("QUESTIONS".equalsIgnoreCase(filter)) {
            type = Discussion.DiscussionType.QUESTION;
        } else if ("UNANSWERED".equalsIgnoreCase(filter)) {
            type = Discussion.DiscussionType.QUESTION;
            status = Discussion.DiscussionStatus.UNANSWERED;
        }

        Page<Discussion> discussions = discussionRepository.findWithFilters(type, status, search, pageable);

        return PageResponse.<DiscussionResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(discussions.getTotalPages())
                .totalElements(discussions.getTotalElements())
                .data(discussions.getContent().stream().map(DiscussionResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public DiscussionResponse replyDiscussion(UUID discussionId, String replyText) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND)); // Or create DISCUSSION_NOT_FOUND

        discussion.setAdminReply(replyText);
        if (discussion.getType() == Discussion.DiscussionType.QUESTION) {
            discussion.setStatus(Discussion.DiscussionStatus.ANSWERED);
        } else {
            discussion.setStatus(Discussion.DiscussionStatus.READ);
        }

        return DiscussionResponse.fromEntity(discussionRepository.save(discussion));
    }

    @Transactional
    public void deleteDiscussion(UUID discussionId) {
        if (!discussionRepository.existsById(discussionId)) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }
        discussionRepository.deleteById(discussionId);
    }

    @Transactional
    public DiscussionResponse likeDiscussion(UUID discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        
        discussion.setLikes(discussion.getLikes() + 1);
        return DiscussionResponse.fromEntity(discussionRepository.save(discussion));
    }
}
