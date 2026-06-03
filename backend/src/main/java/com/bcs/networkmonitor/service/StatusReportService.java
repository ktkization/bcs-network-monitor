package com.bcs.networkmonitor.service;

import com.bcs.networkmonitor.dto.StatusReportRequest;
import com.bcs.networkmonitor.entity.StatusReport;

public interface StatusReportService {
    StatusReport submitReport(Long deviceId, StatusReportRequest request);
}
