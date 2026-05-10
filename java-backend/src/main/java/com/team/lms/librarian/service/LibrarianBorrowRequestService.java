package com.team.lms.librarian.service;

import com.team.lms.librarian.dto.BorrowRequestProcessRequest;
import com.team.lms.librarian.vo.BorrowRequestManageVo;

import java.util.List;

public interface LibrarianBorrowRequestService {
    List<BorrowRequestManageVo> listRequests(String authorizationHeader, String statusFilter);

    BorrowRequestManageVo processRequest(String authorizationHeader, Long requestId, BorrowRequestProcessRequest request);
}
