package com.team.lms.admin.controller;

import com.team.lms.admin.dto.AdminStatusCodeCreateRequest;
import com.team.lms.admin.dto.AdminStatusCodeUpdateRequest;
import com.team.lms.admin.service.AdminRuleService;
import com.team.lms.admin.vo.AdminCategoryCodeVo;
import com.team.lms.admin.vo.AdminStatusCodeVo;
import com.team.lms.common.api.ApiResponse;
import com.team.lms.common.api.BaseController;
import com.team.lms.librarian.dto.CategoryCreateRequest;
import com.team.lms.librarian.dto.CategoryDeleteRequest;
import com.team.lms.librarian.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/rules")
public class AdminRuleController extends BaseController {

    private final AdminRuleService adminRuleService;

    @GetMapping("/category-codes")
    public ApiResponse<List<AdminCategoryCodeVo>> listCategoryCodes(@RequestHeader("Authorization") String authorizationHeader) {
        return success(adminRuleService.listCategoryCodes(authorizationHeader));
    }

    @PostMapping("/category-codes")
    public ApiResponse<AdminCategoryCodeVo> createCategoryCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return success(adminRuleService.createCategoryCode(authorizationHeader, request));
    }

    @PutMapping("/category-codes/{categoryId}")
    public ApiResponse<AdminCategoryCodeVo> updateCategoryCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return success(adminRuleService.updateCategoryCode(authorizationHeader, categoryId, request));
    }

    @DeleteMapping("/category-codes/{categoryId}")
    public ApiResponse<Void> deleteCategoryCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long categoryId,
            @RequestBody(required = false) CategoryDeleteRequest request
    ) {
        adminRuleService.deleteCategoryCode(authorizationHeader, categoryId, request != null && Boolean.TRUE.equals(request.getForce()));
        return success(null);
    }

    @GetMapping("/status-codes")
    public ApiResponse<List<AdminStatusCodeVo>> listStatusCodes(@RequestHeader("Authorization") String authorizationHeader) {
        return success(adminRuleService.listStatusCodes(authorizationHeader));
    }

    @PostMapping("/status-codes")
    public ApiResponse<AdminStatusCodeVo> createStatusCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody AdminStatusCodeCreateRequest request
    ) {
        return success(adminRuleService.createStatusCode(authorizationHeader, request));
    }

    @PutMapping("/status-codes/{statusCodeId}")
    public ApiResponse<AdminStatusCodeVo> updateStatusCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long statusCodeId,
            @Valid @RequestBody AdminStatusCodeUpdateRequest request
    ) {
        return success(adminRuleService.updateStatusCode(authorizationHeader, statusCodeId, request));
    }
}
