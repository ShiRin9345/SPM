package com.team.lms.librarian.dto;

import lombok.Data;

@Data
public class BorrowRequestProcessRequest {
    private Boolean approve;
    private String action;
    private String processNote;
    private String rejectReason;
    private String copyBarcode;

    public boolean isApprove() {
        if (approve != null) {
            return approve;
        }
        if (action == null) {
            return false;
        }
        return "APPROVE".equalsIgnoreCase(action) || "APPROVED".equalsIgnoreCase(action);
    }

    public String effectiveRejectReason() {
        if (rejectReason != null && !rejectReason.isBlank()) {
            return rejectReason.trim();
        }
        if (processNote != null && !processNote.isBlank()) {
            return processNote.trim();
        }
        return null;
    }
}
