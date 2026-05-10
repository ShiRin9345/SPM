package com.team.lms.reader.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReaderFavoriteToggleVo {
    private Long bookId;
    private Boolean favorite;
    private String message;
}
