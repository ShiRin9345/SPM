package com.team.lms.common.controller;

import com.team.lms.common.api.ApiResponse;
import com.team.lms.common.api.BaseController;
import com.team.lms.common.support.PermissionScopeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController extends BaseController {

    private final PermissionScopeSupport permissionScopeSupport;

    @GetMapping("/me")
    public ApiResponse<List<String>> getCurrentPermissions(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return success(permissionScopeSupport.getCurrentPermissions(authorizationHeader));
    }
}
