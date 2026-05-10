package com.team.lms.entity;

import com.team.lms.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookReview extends BaseEntity {
    private User reader;
    private Book book;
    private Integer ratingScore;
    private String reviewContent;
}
