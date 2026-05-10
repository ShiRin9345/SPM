package com.team.lms.librarian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowingRecordManageVo {
    private Long recordId;
    private Long bookId;
    private String bookTitle;
    private String copyBarcode;
    private Long readerId;
    private String readerUsername;
    private String status;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private Long overdueDays;
    private BigDecimal fineAmount;
    private String fineStatus;
    private String reminderInfo;
}
