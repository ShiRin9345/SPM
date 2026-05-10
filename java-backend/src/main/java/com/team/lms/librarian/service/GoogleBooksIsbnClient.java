package com.team.lms.librarian.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.lms.exception.BusinessException;
import com.team.lms.librarian.vo.IsbnLookupVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GoogleBooksIsbnClient {

    private static final String GOOGLE_BOOKS_ENDPOINT = "https://www.googleapis.com/books/v1/volumes?q=isbn:";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public IsbnLookupVo lookup(String isbn) {
        try {
            String normalizedIsbn = isbn == null ? "" : isbn.trim();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_BOOKS_ENDPOINT + URLEncoder.encode(normalizedIsbn, StandardCharsets.UTF_8)))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(502, "isbn lookup request failed");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.path("items");
            if (!items.isArray() || items.isEmpty()) {
                throw new BusinessException(404, "no book metadata found for the given isbn");
            }

            JsonNode volumeInfo = items.get(0).path("volumeInfo");
            return IsbnLookupVo.builder()
                    .isbn(normalizedIsbn)
                    .title(textValue(volumeInfo, "title"))
                    .author(firstArrayText(volumeInfo.path("authors")))
                    .publisher(textValue(volumeInfo, "publisher"))
                    .description(textValue(volumeInfo, "description"))
                    .publishedDate(textValue(volumeInfo, "publishedDate"))
                    .categoryName(firstArrayText(volumeInfo.path("categories")))
                    .thumbnailUrl(textValue(volumeInfo.path("imageLinks"), "thumbnail"))
                    .build();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(502, "failed to fetch isbn metadata");
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private String firstArrayText(JsonNode node) {
        if (!node.isArray() || node.isEmpty()) {
            return null;
        }
        JsonNode value = node.get(0);
        return value == null || value.isNull() ? null : value.asText(null);
    }
}
