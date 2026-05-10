package com.team.lms.admin.service;

import com.team.lms.admin.dto.AdminStatusCodeCreateRequest;
import com.team.lms.admin.dto.AdminStatusCodeUpdateRequest;
import com.team.lms.admin.vo.AdminCategoryCodeVo;
import com.team.lms.admin.vo.AdminStatusCodeVo;
import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;

import java.util.List;

public interface AdminRuleService {
    List<AdminCategoryCodeVo> listCategoryCodes(String authorizationHeader);
    AdminCategoryCodeVo createCategoryCode(String authorizationHeader, CategoryCreateRequest request);
    AdminCategoryCodeVo updateCategoryCode(String authorizationHeader, Long categoryId, CategoryUpdateRequest request);
    void deleteCategoryCode(String authorizationHeader, Long categoryId, boolean force);
    List<AdminStatusCodeVo> listStatusCodes(String authorizationHeader);
    AdminStatusCodeVo createStatusCode(String authorizationHeader, AdminStatusCodeCreateRequest request);
    AdminStatusCodeVo updateStatusCode(String authorizationHeader, Long statusCodeId, AdminStatusCodeUpdateRequest request);
}
