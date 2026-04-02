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
}
