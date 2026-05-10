package com.team.lms.librarian.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReturnManageVo {
    private Long recordId;
    private Long bookId;
    private String bookTitle;
    private String copyBarcode;
    private Long readerId;
    private String readerUsername;
    private String status;
    private String dueDate;
    private String returnDate;
    private BigDecimal fineAmount;
    private String message;
}
