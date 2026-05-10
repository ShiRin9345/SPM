package com.team.lms.librarian.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IsbnLookupVo {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private String publishedDate;
    private String categoryName;
    private String thumbnailUrl;
}
