package com.team.lms.librarian.controller;

import com.team.lms.common.api.ApiResponse;
import com.team.lms.common.api.BaseController;
import com.team.lms.librarian.dto.BorrowRequestProcessRequest;
import com.team.lms.librarian.service.LibrarianBorrowRequestService;
import com.team.lms.librarian.vo.BorrowRequestManageVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/librarian/borrow-requests")
public class LibrarianBorrowRequestController extends BaseController {

    private final LibrarianBorrowRequestService librarianBorrowRequestService;

    @GetMapping
    public ApiResponse<List<BorrowRequestManageVo>> listPendingRequests(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(value = "status_filter", required = false) String statusFilter
    ) {
        return success(librarianBorrowRequestService.listRequests(authorizationHeader, statusFilter));
    }

    @PostMapping("/{requestId}/process")
    public ApiResponse<BorrowRequestManageVo> processRequest(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long requestId,
            @Valid @RequestBody BorrowRequestProcessRequest request
    ) {
        return success(librarianBorrowRequestService.processRequest(authorizationHeader, requestId, request));
    }
}
