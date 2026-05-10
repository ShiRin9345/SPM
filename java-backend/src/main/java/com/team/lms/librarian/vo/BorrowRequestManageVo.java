package com.team.lms.librarian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BorrowRequestManageVo {
    private Long requestId;
    private Long bookId;
    private String bookTitle;
    private String copyBarcode;
    private Long readerId;
    private String readerUsername;
    private String status;
    private String requestedAt;
    private Integer remainingCopies;
    private String message;
}
