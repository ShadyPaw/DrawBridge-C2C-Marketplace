package com.cargoco.service;

import com.cargoco.common.PageResult;
import com.cargoco.entity.Report;

public interface ReportService {
    Report submit(Report report);
    Report handle(Long id, Integer handleStatus, String handleResult, Long handleUserId);
    PageResult<Report> getList(Integer handleStatus, Integer targetType, Integer pageNum, Integer pageSize);
    Long countByStatus(Integer handleStatus);
}
