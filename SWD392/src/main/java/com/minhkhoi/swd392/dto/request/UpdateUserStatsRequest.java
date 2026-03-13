package com.minhkhoi.swd392.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatsRequest {
    /** Thêm XP vào tài khoản (delta, không phải giá trị tuyệt đối) */
    private Integer xpDelta;

    /** Set streak trực tiếp (ngày liên tục) */
    private Integer streak;

    /** Số badge mới (dùng khi mở badge mới) */
    private Integer totalBadges;
}
