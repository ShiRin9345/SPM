package com.team.lms.reader.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReaderBookReviewCreateRequest {
    @NotNull(message = "ratingScore is required")
    @Min(value = 1, message = "ratingScore must be between 1 and 5")
    @Max(value = 5, message = "ratingScore must be between 1 and 5")
    private Integer ratingScore;

    @NotBlank(message = "reviewContent is required")
    private String reviewContent;
}
