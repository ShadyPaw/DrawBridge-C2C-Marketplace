package com.cargoco.mapper;

import com.cargoco.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportMapper {
    Report findById(Long id);
    int insert(Report report);
    int update(Report report);
    List<Report> findList(@Param("handleStatus") Integer handleStatus, @Param("targetType") Integer targetType);
    Long countByStatus(@Param("handleStatus") Integer handleStatus);
    Long countByTarget(@Param("targetId") Long targetId, @Param("targetType") Integer targetType);
    /**
     * 全域举报聚合：统计该用户作为目标、其商品作为目标、其消息作为目标的所有举报总数
     */
    Long countGlobalReports(@Param("userId") Long userId);
}
