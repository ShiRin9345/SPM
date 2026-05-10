package com.team.lms.librarian.controller;

import com.team.lms.common.api.ApiResponse;
import com.team.lms.common.api.BaseController;
import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryDeleteRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;
import com.team.lms.librarian.service.LibrarianCategoryService;
import com.team.lms.librarian.vo.CategoryManageVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/librarian/categories")
public class LibrarianCategoryController extends BaseController {

    private final LibrarianCategoryService librarianCategoryService;

    @GetMapping
    public ApiResponse<List<CategoryManageVo>> listCategories(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return success(librarianCategoryService.listCategories(authorizationHeader));
    }

    @PostMapping
    public ApiResponse<CategoryManageVo> createCategory(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return success(librarianCategoryService.createCategory(authorizationHeader, request));
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryManageVo> updateCategory(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return success(librarianCategoryService.updateCategory(authorizationHeader, categoryId, request));
    }
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long categoryId,
            @RequestBody(required = false) CategoryDeleteRequest request
    ) {
        boolean force = request != null && Boolean.TRUE.equals(request.getForce());
        librarianCategoryService.deleteCategory(authorizationHeader, categoryId, force);
        return success(null);
    }
}
