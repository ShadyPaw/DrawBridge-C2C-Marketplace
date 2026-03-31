package com.cargoco.mapper;

import com.cargoco.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NoticeMapper {
    Notice findById(Long id);
    List<Notice> findByTargetUserId(@Param("targetUserId") Long targetUserId);
    List<Notice> findSystemNotices();
    List<Notice> findAll(@Param("type") Integer type);
    int insert(Notice notice);
    int update(Notice notice);
    int deleteById(Long id);
    int markRead(Long id);
    int markAllRead(Long targetUserId);
    Long countUnread(Long targetUserId);
}
