package com.team.lms.librarian.service;

import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;
import com.team.lms.librarian.vo.CategoryManageVo;

import java.util.List;

public interface LibrarianCategoryService {
    List<CategoryManageVo> listCategories(String authorizationHeader);
    CategoryManageVo createCategory(String authorizationHeader, CategoryCreateRequest request);
    CategoryManageVo updateCategory(String authorizationHeader, Long categoryId, CategoryUpdateRequest request);
    void deleteCategory(String authorizationHeader, Long categoryId, boolean force);
}
