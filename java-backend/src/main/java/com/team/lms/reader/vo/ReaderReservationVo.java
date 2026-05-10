package com.team.lms.reader.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReaderReservationVo {
    private Long reservationId;
    private Long bookId;
    private String bookTitle;
    private String status;
    private Integer queueNo;
    private String createdAt;
    private String message;
}
