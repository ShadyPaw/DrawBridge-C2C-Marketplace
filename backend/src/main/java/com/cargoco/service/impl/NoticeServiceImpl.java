package com.cargoco.service.impl;

import com.cargoco.common.PageResult;
import com.cargoco.entity.Notice;
import com.cargoco.mapper.NoticeMapper;
import com.cargoco.service.NoticeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public List<Notice> getByTargetUserId(Long targetUserId) {
        return noticeMapper.findByTargetUserId(targetUserId);
    }

    @Override
    public List<Notice> getSystemNotices() {
        return noticeMapper.findSystemNotices();
    }

    @Override
    public PageResult<Notice> getAll(Integer type, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notice> list = noticeMapper.findAll(type);
        PageInfo<Notice> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList(), pageNum, pageSize);
    }

    @Override
    public Notice add(Notice notice) {
        notice.setStatus(1);
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public Notice update(Notice notice) {
        noticeMapper.update(notice);
        return noticeMapper.findById(notice.getId());
    }

    @Override
    public void delete(Long id) { noticeMapper.deleteById(id); }

    @Override
    public void markRead(Long id) { noticeMapper.markRead(id); }

    @Override
    public void markAllRead(Long targetUserId) { noticeMapper.markAllRead(targetUserId); }

    @Override
    public Long countUnread(Long targetUserId) { return noticeMapper.countUnread(targetUserId); }
}
