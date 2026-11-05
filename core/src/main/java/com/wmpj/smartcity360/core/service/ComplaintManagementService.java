package com.wmpj.smartcity360.core.service;

import com.wmpj.smartcity360.core.dto.ComplaintHistorySummary;
import com.wmpj.smartcity360.core.dto.ComplaintSubmission;
import com.wmpj.smartcity360.core.dto.ComplaintSummary;
import com.wmpj.smartcity360.core.dto.PageRequest;
import com.wmpj.smartcity360.core.dto.PageResponse;

import java.util.List;

public interface ComplaintManagementService {

    ComplaintSummary submit(ComplaintSubmission submission);

    PageResponse<ComplaintSummary> list(PageRequest pageRequest);

    ComplaintSummary get(long complaintId);

    List<ComplaintHistorySummary> getHistory(long complaintId);

    ComplaintSummary route(long complaintId);

    ComplaintSummary escalate(long complaintId, String reason);

    ComplaintSummary resolve(long complaintId, String notes);
}
