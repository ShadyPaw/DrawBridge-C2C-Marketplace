package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.Report;
import com.cargoco.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 举报功能控制器
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     * 
     * @param report 举报信息
     * @param request 请求对象，用于获取当前登录用户 ID
     * @return 结果
     */
    @PostMapping("/submit")
    public Result<Report> submit(@RequestBody Report report, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("请先登录");
            }
            report.setReporterId(userId);
            Report savedReport = reportService.submit(report);
            return Result.success("举报提交成功，系统将通过 AI 进行实时风险研判", savedReport);
        } catch (Exception e) {
            return Result.error("提交失败：" + e.getMessage());
        }
    }
}
