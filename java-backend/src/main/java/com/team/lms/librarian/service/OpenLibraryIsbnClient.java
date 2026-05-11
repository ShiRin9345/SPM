package com.team.lms.librarian.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.lms.exception.BusinessException;
import com.team.lms.librarian.vo.IsbnLookupVo;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class OpenLibraryIsbnClient {

    private static final String OPEN_LIBRARY_ENDPOINT =
            "https://openlibrary.org/api/books?bibkeys=ISBN:%s&format=json&jscmd=data";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(15))
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 3067)))
            .build();

    public IsbnLookupVo lookup(String isbn) {
        String normalizedIsbn = isbn == null ? "" : isbn.trim();
        String url = String.format(OPEN_LIBRARY_ENDPOINT, URLEncoder.encode(normalizedIsbn, StandardCharsets.UTF_8));
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(502, "isbn lookup request failed");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String key = "ISBN:" + normalizedIsbn;
            JsonNode bookData = root.path(key);

            if (bookData.isMissingNode()) {
                throw new BusinessException(404, "no book found for the given isbn");
            }

            return IsbnLookupVo.builder()
                    .isbn(normalizedIsbn)
                    .title(textValue(bookData, "title"))
                    .author(firstAuthorName(bookData.path("authors")))
                    .publisher(firstPublisherName(bookData.path("publishers")))
                    .description(extractDescription(bookData))
                    .publishedDate(textValue(bookData, "publish_date"))
                    .categoryName(firstSubject(bookData.path("subjects")))
                    .thumbnailUrl(extractCoverUrl(bookData.path("cover")))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(502, "failed to fetch isbn metadata: " + e.getMessage());
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private String firstAuthorName(JsonNode authors) {
        if (authors.isArray() && !authors.isEmpty()) {
            JsonNode first = authors.get(0);
            return first.path("name").asText(null);
        }
        return null;
    }

    private String firstPublisherName(JsonNode publishers) {
        if (publishers.isArray() && !publishers.isEmpty()) {
            String name = publishers.get(0).path("name").asText(null);
            if (name != null) return name;
            return publishers.get(0).asText(null);
        }
        return null;
    }

    private String extractDescription(JsonNode bookData) {
        String desc = textValue(bookData, "by_statement");
        if (desc != null) return desc;
        JsonNode notes = bookData.path("notes");
        if (notes.isArray() && !notes.isEmpty()) {
            return notes.get(0).asText(null);
        }
        return null;
    }

    private String firstSubject(JsonNode subjects) {
        if (subjects.isArray() && !subjects.isEmpty()) {
            JsonNode first = subjects.get(0);
            String name = first.path("name").asText(null);
            if (name != null) return name;
            return first.asText(null);
        }
        return null;
    }

    private String extractCoverUrl(JsonNode cover) {
        if (cover.isMissingNode()) return null;
        return cover.path("medium").asText(null);
    }
}
