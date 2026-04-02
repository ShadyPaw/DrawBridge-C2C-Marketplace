package com.cargoco.mapper;

import com.cargoco.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    /** 插入一条聊天消息 */
    int insert(ChatMessage chatMessage);

    /** 查询两个用户之间的历史聊天记录（按时间升序） */
    List<ChatMessage> findConversation(@Param("userId1") Long userId1,
                                       @Param("userId2") Long userId2,
                                       @Param("productId") Long productId);

    /** 查询某个用户的最近联系人列表（每个会话取最新一条消息） */
    List<ChatMessage> findRecentContacts(@Param("userId") Long userId);

    /** 将某个会话的未读消息标记为已读 */
    int markAsRead(@Param("senderId") Long senderId,
                   @Param("receiverId") Long receiverId);

    /** 查询某用户的未读消息总数 */
    int countUnread(@Param("userId") Long userId);

    /** 根据ID查询单条消息记录 */
    ChatMessage findById(@Param("id") Long id);
}
