package com.team.lms.reader.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReaderBookReviewVo {
    private Long reviewId;
    private String readerUsername;
    private Integer ratingScore;
    private String reviewContent;
    private String createdAt;
    private Boolean mine;
}
