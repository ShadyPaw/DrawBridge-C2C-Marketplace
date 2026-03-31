package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Report;
import com.cargoco.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/submit")
    public Result<Report> submit(@RequestBody Report report, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        report.setReporterId(userId);
        Report submitted = reportService.submit(report);
        return Result.success("举报成功，我们将尽快处理", submitted);
    }
}
