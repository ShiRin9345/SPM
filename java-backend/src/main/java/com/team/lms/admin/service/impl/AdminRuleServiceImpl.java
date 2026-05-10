package com.team.lms.admin.service.impl;

import com.team.lms.admin.dto.AdminStatusCodeCreateRequest;
import com.team.lms.admin.dto.AdminStatusCodeUpdateRequest;
import com.team.lms.admin.service.AdminRuleService;
import com.team.lms.admin.vo.AdminCategoryCodeVo;
import com.team.lms.admin.vo.AdminStatusCodeVo;
import com.team.lms.common.enums.RoleType;
import com.team.lms.common.support.CurrentUserSupport;
import com.team.lms.common.support.OperationLogSupport;
import com.team.lms.entity.Category;
import com.team.lms.entity.StatusCode;
import com.team.lms.entity.User;
import com.team.lms.exception.BusinessException;
import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;
import com.team.lms.mapper.BookMapper;
import com.team.lms.mapper.CategoryMapper;
import com.team.lms.mapper.StatusCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminRuleServiceImpl implements AdminRuleService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;
    private final StatusCodeMapper statusCodeMapper;
    private final CurrentUserSupport currentUserSupport;
    private final OperationLogSupport operationLogSupport;

    @Override
    public List<AdminCategoryCodeVo> listCategoryCodes(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return categoryMapper.selectAll().stream().map(this::toCategoryVo).toList();
    }

    @Override
    @Transactional
    public AdminCategoryCodeVo createCategoryCode(String authorizationHeader, CategoryCreateRequest request) {
        User admin = requireAdmin(authorizationHeader);
        String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (categoryMapper.selectByCode(normalizedCode) != null) {
            throw new BusinessException(400, "category code already exists");
        }
        Category category = new Category();
        category.setCode(normalizedCode);
        category.setName(request.getName().trim());
        category.setEnabled(request.getEnabled());
        categoryMapper.insert(category);
        operationLogSupport.record("SYSTEM_RULE", "CATEGORY_CODE_CREATE", admin.getUsername(), "Created category code " + normalizedCode);
        return toCategoryVo(categoryMapper.selectById(category.getId()));
    }

    @Override
    @Transactional
    public AdminCategoryCodeVo updateCategoryCode(String authorizationHeader, Long categoryId, CategoryUpdateRequest request) {
        User admin = requireAdmin(authorizationHeader);
        Category category = requireCategory(categoryId);
        category.setName(request.getName().trim());
        category.setEnabled(request.getEnabled());
        categoryMapper.update(category);
        operationLogSupport.record("SYSTEM_RULE", "CATEGORY_CODE_UPDATE", admin.getUsername(), "Updated category code " + category.getCode());
        return toCategoryVo(categoryMapper.selectById(categoryId));
    }

    @Override
    @Transactional
    public void deleteCategoryCode(String authorizationHeader, Long categoryId, boolean force) {
        User admin = requireAdmin(authorizationHeader);
        Category category = requireCategory(categoryId);
        long bookCount = bookMapper.countByCategoryId(categoryId);
        if (bookCount > 0 && !force) {
            throw new BusinessException(400, "category is still referenced by books");
        }
        if (bookCount > 0) {
            bookMapper.clearCategoryByCategoryId(categoryId);
        }
        category.setDeleted(true);
        categoryMapper.update(category);
        operationLogSupport.record("SYSTEM_RULE", "CATEGORY_CODE_DELETE", admin.getUsername(), "Deleted category code " + category.getCode());
    }

    @Override
    public List<AdminStatusCodeVo> listStatusCodes(String authorizationHeader) {
        requireAdmin(authorizationHeader);
        return statusCodeMapper.selectAll().stream().map(this::toStatusCodeVo).toList();
    }

    @Override
    public AdminStatusCodeVo createStatusCode(String authorizationHeader, AdminStatusCodeCreateRequest request) {
        User admin = requireAdmin(authorizationHeader);
        String codeType = request.getCodeType().trim().toUpperCase(Locale.ROOT);
        String codeValue = request.getCodeValue().trim().toUpperCase(Locale.ROOT);
        if (statusCodeMapper.selectByTypeAndValue(codeType, codeValue) != null) {
            throw new BusinessException(400, "status code already exists");
        }
        StatusCode statusCode = new StatusCode();
        statusCode.setCodeType(codeType);
        statusCode.setCodeValue(codeValue);
        statusCode.setDisplayName(request.getDisplayName().trim());
        statusCode.setDescription(blankToNull(request.getDescription()));
        statusCode.setEnabled(request.getEnabled());
        statusCodeMapper.insert(statusCode);
        operationLogSupport.record("SYSTEM_RULE", "STATUS_CODE_CREATE", admin.getUsername(), "Created status code " + codeType + ":" + codeValue);
        return toStatusCodeVo(statusCodeMapper.selectById(statusCode.getId()));
    }

    @Override
    public AdminStatusCodeVo updateStatusCode(String authorizationHeader, Long statusCodeId, AdminStatusCodeUpdateRequest request) {
        User admin = requireAdmin(authorizationHeader);
        StatusCode statusCode = requireStatusCode(statusCodeId);
        statusCode.setDisplayName(request.getDisplayName().trim());
        statusCode.setDescription(blankToNull(request.getDescription()));
        statusCode.setEnabled(request.getEnabled());
        statusCodeMapper.update(statusCode);
        operationLogSupport.record("SYSTEM_RULE", "STATUS_CODE_UPDATE", admin.getUsername(), "Updated status code " + statusCode.getCodeType() + ":" + statusCode.getCodeValue());
        return toStatusCodeVo(statusCodeMapper.selectById(statusCodeId));
    }

    private User requireAdmin(String authorizationHeader) {
        User user = currentUserSupport.requireUser(authorizationHeader);
        if (user.getRole() != RoleType.ADMIN) {
            throw new BusinessException(403, "current user is not an admin");
        }
        return user;
    }

    private Category requireCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "category not found");
        }
        return category;
    }

    private StatusCode requireStatusCode(Long statusCodeId) {
        StatusCode statusCode = statusCodeMapper.selectById(statusCodeId);
        if (statusCode == null) {
            throw new BusinessException(404, "status code not found");
        }
        return statusCode;
    }

    private AdminCategoryCodeVo toCategoryVo(Category category) {
        return AdminCategoryCodeVo.builder()
                .categoryId(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .enabled(category.getEnabled())
                .build();
    }

    private AdminStatusCodeVo toStatusCodeVo(StatusCode statusCode) {
        return AdminStatusCodeVo.builder()
                .statusCodeId(statusCode.getId())
                .codeType(statusCode.getCodeType())
                .codeValue(statusCode.getCodeValue())
                .displayName(statusCode.getDisplayName())
                .description(statusCode.getDescription())
                .enabled(statusCode.getEnabled())
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
