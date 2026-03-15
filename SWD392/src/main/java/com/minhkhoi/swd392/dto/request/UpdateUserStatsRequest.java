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

    private Integer xpDelta;

    private Integer streak;

    private Integer totalBadges;
}
