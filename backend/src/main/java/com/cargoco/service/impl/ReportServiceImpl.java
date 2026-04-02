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
    private com.cargoco.mapper.ChatMessageMapper chatMessageMapper;
    @Autowired
    private ReportMapper reportMapper;

    @org.springframework.beans.factory.annotation.Value("${risk.model.suspend-threshold:0.85}")
    private Float suspendThreshold;

    @Override
    public Report submit(Report report) {
        report.setHandleStatus(0);
        report.setCreateTime(new Date());
        reportMapper.insert(report);

        // --- 全域实时研判 (Trigger AI Task) ---
        // 废除旧版“双轨制”，任何来源（商品、用户、聊天）的举报都将计入全站负向特征并触发 AI 重算
        Long targetUserId = null;
        if (report.getTargetType() == 2) {
            // 举报用户
            targetUserId = report.getTargetId();
        } else if (report.getTargetType() == 1) {
            // 举报商品 -> 找到卖家
            com.cargoco.entity.Product product = productService.getDetail(report.getTargetId());
            if (product != null) targetUserId = product.getUserId();
        } else if (report.getTargetType() == 3) {
            // 举报私聊消息 -> 找到消息发送者
            com.cargoco.entity.ChatMessage msg = chatMessageMapper.findById(report.getTargetId());
            if (msg != null) targetUserId = msg.getSenderId();
        }

        if (targetUserId != null) {
            com.cargoco.entity.User user = userService.getUserById(targetUserId);
            if (user != null) {
                // 1. 注册天数
                float registerDays = (float) ((System.currentTimeMillis() - user.getCreateTime().getTime()) / (1000 * 60 * 60 * 24));
                // 2. 商品总数
                float productCount = (float) productService.getByUserId(targetUserId).size();
                // 3. 全域举报总数 (来自用户页、商品页、私聊界面的全量聚合)
                float reportCount = reportMapper.countGlobalReports(targetUserId).floatValue(); 
                // 4. 平均回复耗时 (Mock)
                float avgReplyTime = 2.0f; 
                // 5. 张量伪装：硬编码旧版信用分为 100.0f 以适配模型输入层
                float creditScore = 100.0f;

                // 执行实时 AI 风险推理
                float newRiskScore = riskInferenceService.predictRiskScore(registerDays, productCount, reportCount, avgReplyTime, creditScore);
                
                // 更新用户的 AI 风险分 (非持久化到 User 表，通常在查询时注入或实时计算)
                // 这里我们根据风险阈值直接采取自动化熔断措施
                if (newRiskScore > suspendThreshold) {
                    // 自动锁定账号
                    userMapper.updateStatus(targetUserId, 0);

                    // 发送强力风控通知
                    com.cargoco.entity.Notice notice = new com.cargoco.entity.Notice();
                    notice.setSenderId(0L);
                    notice.setTargetUserId(targetUserId);
                    notice.setType(3); // 审核/风控通知
                    notice.setTitle("【安全监测】您的账号已被限制访问");
                    notice.setContent("系统检测到您的全域行为风险指数过高 (" + String.format("%.1f", newRiskScore * 100) + "%)。根据平台风控协议，您的账号现已被采取保护性锁定措施。请登录平台官网或联系客服申诉。");
                    noticeService.add(notice);
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
