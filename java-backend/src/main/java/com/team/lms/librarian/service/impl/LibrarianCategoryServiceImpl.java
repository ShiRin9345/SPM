package com.team.lms.librarian.service.impl;

import com.team.lms.common.enums.RoleType;
import com.team.lms.common.support.PermissionScopeSupport;
import com.team.lms.entity.Category;
import com.team.lms.exception.BusinessException;
import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;
import com.team.lms.librarian.service.LibrarianCategoryService;
import com.team.lms.librarian.vo.CategoryManageVo;
import com.team.lms.mapper.BookMapper;
import com.team.lms.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibrarianCategoryServiceImpl implements LibrarianCategoryService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;
    private final PermissionScopeSupport permissionScopeSupport;

    @Override
    public List<CategoryManageVo> listCategories(String authorizationHeader) {
        permissionScopeSupport.requirePermission(authorizationHeader, RoleType.LIBRARIAN, "BOOK_MANAGE");
        return categoryMapper.selectAll().stream().map(this::toManageVo).toList();
    }

    @Override
    @Transactional
    public CategoryManageVo createCategory(String authorizationHeader, CategoryCreateRequest request) {
        permissionScopeSupport.requirePermission(authorizationHeader, RoleType.LIBRARIAN, "BOOK_MANAGE");
        String normalizedCode = request.getCode().trim().toUpperCase();
        if (categoryMapper.selectByCode(normalizedCode) != null) {
            throw new BusinessException(400, "category code already exists");
        }

        Category category = new Category();
        category.setCode(normalizedCode);
        category.setName(request.getName().trim());
        category.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        categoryMapper.insert(category);

        return toManageVo(category);
    }

    @Override
    @Transactional
    public CategoryManageVo updateCategory(String authorizationHeader, Long categoryId, CategoryUpdateRequest request) {
        permissionScopeSupport.requirePermission(authorizationHeader, RoleType.LIBRARIAN, "BOOK_MANAGE");
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "category not found");
        }
        category.setName(request.getName().trim());
        if (request.getEnabled() != null) {
            category.setEnabled(request.getEnabled());
        }
        categoryMapper.update(category);
        return toManageVo(categoryMapper.selectById(categoryId));
    }

    private CategoryManageVo toManageVo(Category category) {
        return CategoryManageVo.builder()
                .categoryId(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .enabled(category.getEnabled())
                .build();
    }
    @Override
    @Transactional
    public void deleteCategory(String authorizationHeader, Long categoryId, boolean force) {
        permissionScopeSupport.requirePermission(authorizationHeader, RoleType.LIBRARIAN, "BOOK_MANAGE");
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "category not found");
        }

        long bookCount = bookMapper.countByCategoryId(categoryId);
        if (bookCount > 0 && !force) {
            throw new BusinessException(400, "category has " + bookCount + " books associated, use force=true to delete anyway");
        }

        if (force && bookCount > 0) {
            bookMapper.clearCategoryByCategoryId(categoryId);
        }

        categoryMapper.softDeleteById(categoryId);
    }
}
