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
    private ReportMapper reportMapper;

    @Override
    public Report submit(Report report) {
        report.setHandleStatus(0);
        reportMapper.insert(report);
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
