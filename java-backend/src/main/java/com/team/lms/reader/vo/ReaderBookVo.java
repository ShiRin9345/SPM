package com.team.lms.reader.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReaderBookVo {
    private Long bookId;
    private String title;
    private String author;
    private String categoryName;
    private Integer availableCopies;
    private String shelfStatus;
    private Boolean favorite;
    private Double averageRating;
}
