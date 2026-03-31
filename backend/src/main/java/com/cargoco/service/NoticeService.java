package com.cargoco.service;

import com.cargoco.entity.Notice;
import com.cargoco.common.PageResult;
import java.util.List;

public interface NoticeService {
    List<Notice> getByTargetUserId(Long targetUserId);
    List<Notice> getSystemNotices();
    PageResult<Notice> getAll(Integer type, Integer pageNum, Integer pageSize);
    Notice add(Notice notice);
    Notice update(Notice notice);
    void delete(Long id);
    void markRead(Long id);
    void markAllRead(Long targetUserId);
    Long countUnread(Long targetUserId);
}
