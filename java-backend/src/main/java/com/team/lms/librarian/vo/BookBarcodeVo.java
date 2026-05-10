package com.team.lms.librarian.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookBarcodeVo {
    private Long bookId;
    private String isbn;
    private Integer copyCount;
    private List<String> barcodes;
}
