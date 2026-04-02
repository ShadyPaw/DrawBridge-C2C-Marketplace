package com.cargoco.service.impl;

import com.cargoco.common.PageResult;
import com.cargoco.entity.Report;
import com.cargoco.mapper.ReportMapper;
import com.cargoco.service.ReportService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private com.cargoco.service.RiskInferenceService riskInferenceService;

    @Autowired
    private com.cargoco.service.UserService userService;

    @Autowired
    private com.cargoco.service.ProductService productService;

    @Autowired
    private com.cargoco.service.NoticeService noticeService;

    @Autowired
    private com.cargoco.mapper.UserMapper userMapper;
    @Autowired
    private ReportMapper reportMapper;

    @Override
    public Report submit(Report report) {
        report.setHandleStatus(0);
        report.setCreateTime(new Date());
        reportMapper.insert(report);

        // --- 实时研判 (Trigger AI Task) ---
        // 【双轨制风控】: 只有【交易性举报】(reportType=1) 才重算风险，其余（资料 2、聊天 3 等内容违规）物理级隔离。
        if (report.getReportType() != null && report.getReportType() == 1) {
            Long targetUserId = null;
            if (report.getTargetType() == 2) {
                targetUserId = report.getTargetId();
            } else if (report.getTargetType() == 1) {
                com.cargoco.entity.Product product = productService.getDetail(report.getTargetId());
                if (product != null) targetUserId = product.getUserId();
            }

            if (targetUserId != null) {
                com.cargoco.entity.User user = userService.getUserById(targetUserId);
                if (user != null) {
                    // 重新计算该用户的 5 个特征数据
                    float registerDays = (float) ((System.currentTimeMillis() - user.getCreateTime().getTime()) / (1000 * 60 * 60 * 24));
                    float productCount = (float) productService.getByUserId(targetUserId).size();
                    // 实时查询该用户被举报的次数 (包含刚才那一次)
                    float reportCount = reportMapper.countByTarget(targetUserId, 2).floatValue(); 
                    float avgReplyTime = 2.0f; // Mock
                    float creditScore = user.getCreditScore().floatValue();

                    // 执行 AI 推理
                    float newRiskScore = riskInferenceService.predictRiskScore(registerDays, productCount, reportCount, avgReplyTime, creditScore);
                    
                    // 自动化逻辑: 如果分值极高 ( > 0.9 )，自动采取封禁/限制措施
                    if (newRiskScore > 0.9f) {
                        // status=0 表示锁定/禁用
                        userMapper.updateStatus(targetUserId, 0);

                        // 发送系统处理通知
                        com.cargoco.entity.Notice notice = new com.cargoco.entity.Notice();
                        notice.setSenderId(0L); // 系统发送
                        notice.setTargetUserId(targetUserId);
                        notice.setType(1); // 系统消息
                        notice.setTitle("【AI 风险预警】账号安全限制通知");
                        notice.setContent("由于系统监测到近期您的账号异常行为较多 (AI 风险指数: " + String.format("%.1f", newRiskScore * 100) + "%)，现已对您的账号进行锁定限制。如有异议，请联系平台人工客服进行申诉。");
                        noticeService.add(notice);
                    }
                }
            }
        }
        return report;
    }

    @Override
    public Report handle(Long id, Integer handleStatus, String handleResult, Long handleUserId) {
        Report report = reportMapper.findById(id);
        if (report == null) throw new RuntimeException("举报不存在");
        report.setHandleStatus(handleStatus);
        report.setHandleResult(handleResult);
        report.setHandleUserId(handleUserId);
        report.setHandleTime(new Date());
        reportMapper.update(report);
        return reportMapper.findById(id);
    }

    @Override
    public PageResult<Report> getList(Integer handleStatus, Integer targetType, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Report> list = reportMapper.findList(handleStatus, targetType);
        PageInfo<Report> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public Long countByStatus(Integer handleStatus) {
        return reportMapper.countByStatus(handleStatus);
    }
}
