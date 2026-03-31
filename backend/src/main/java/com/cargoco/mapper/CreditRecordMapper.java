package com.cargoco.mapper;

import com.cargoco.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CreditRecordMapper {
    int insert(CreditRecord record);
    List<CreditRecord> findByUserId(Long userId);
}
