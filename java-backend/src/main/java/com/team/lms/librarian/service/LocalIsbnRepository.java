package com.team.lms.librarian.service;

import com.team.lms.librarian.vo.IsbnLookupVo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LocalIsbnRepository {

    private static final Map<String, IsbnLookupVo> DATABASE = Map.ofEntries(
            entry("9780134494166", "Clean Architecture", "Robert C. Martin", "Prentice Hall",
                    "A Craftsman's Guide to Software Structure and Design", "2017", "Computer Science"),
            entry("9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall",
                    "A Handbook of Agile Software Craftsmanship", "2008", "Computer Science"),
            entry("9780201616224", "The Pragmatic Programmer", "Andrew Hunt, David Thomas", "Addison-Wesley",
                    "From Journeyman to Master", "1999", "Computer Science"),
            entry("9781492078005", "Designing Data-Intensive Applications", "Martin Kleppmann", "O'Reilly Media",
                    "The Big Ideas Behind Reliable, Scalable, and Maintainable Systems", "2017", "Computer Science"),
            entry("9781617296277", "Spring Boot in Action", "Craig Walls", "Manning Publications",
                    "A practical guide to building Spring Boot applications", "2021", "Computer Science"),
            entry("9787111638333", "Effective Java (3rd Edition)", "Joshua Bloch", "Addison-Wesley",
                    "Best practices for the Java platform", "2018", "Computer Science"),
            entry("9787115546081", "Computer Networking: A Top-Down Approach", "James Kurose, Keith Ross", "Pearson",
                    "A comprehensive textbook on computer networking", "2017", "Computer Science"),
            entry("9787115428028", "Introduction to Algorithms (3rd Edition)", "Thomas H. Cormen et al.", "MIT Press",
                    "The definitive guide to algorithms", "2009", "Computer Science"),
            entry("9787121385407", "Design Patterns", "Erich Gamma et al.", "Addison-Wesley",
                    "Elements of Reusable Object-Oriented Software", "1994", "Computer Science"),
            entry("9787115293800", "JavaScript: The Good Parts", "Douglas Crockford", "O'Reilly Media",
                    "Unearthing the Excellence in JavaScript", "2008", "Computer Science")
    );

    public IsbnLookupVo find(String isbn) {
        return DATABASE.get(isbn == null ? "" : isbn.trim());
    }

    private static Map.Entry<String, IsbnLookupVo> entry(
            String isbn, String title, String author, String publisher,
            String description, String publishedDate, String categoryName) {
        return Map.entry(isbn, IsbnLookupVo.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .publisher(publisher)
                .description(description)
                .publishedDate(publishedDate)
                .categoryName(categoryName)
                .build());
    }
}
