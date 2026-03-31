package com.cargoco.mapper;

import com.cargoco.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper {
    Message findById(Long id);
    List<Message> findByProductId(Long productId);
    List<Message> findTopLevelByProductId(Long productId);
    List<Message> findReplies(Long parentId);
    int insert(Message message);
    int deleteById(Long id);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
