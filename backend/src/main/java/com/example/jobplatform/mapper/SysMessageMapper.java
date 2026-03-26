package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysMessageMapper {

    @Select("""
            SELECT id, receiver_user_id, sender_user_id, message_type, title, content,
                   related_business_type, related_business_id, is_read, read_time, created_at
            FROM sys_message
            WHERE id = #{id}
            """)
    SysMessage selectById(Long id);

    @Select("""
            SELECT id, receiver_user_id, sender_user_id, message_type, title, content,
                   related_business_type, related_business_id, is_read, read_time, created_at
            FROM sys_message
            WHERE receiver_user_id = #{receiverUserId}
            ORDER BY is_read ASC, created_at DESC, id DESC
            """)
    List<SysMessage> selectByReceiverUserId(Long receiverUserId);

    @Select("""
            SELECT id, receiver_user_id, sender_user_id, message_type, title, content,
                   related_business_type, related_business_id, is_read, read_time, created_at
            FROM sys_message
            ORDER BY id DESC
            """)
    List<SysMessage> selectAll();

    @Insert("""
            INSERT INTO sys_message (
                receiver_user_id, sender_user_id, message_type, title, content,
                related_business_type, related_business_id, is_read, read_time
            ) VALUES (
                #{receiverUserId}, #{senderUserId}, #{messageType}, #{title}, #{content},
                #{relatedBusinessType}, #{relatedBusinessId}, #{isRead}, #{readTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMessage sysMessage);

    @Update("""
            <script>
            UPDATE sys_message
            <set>
                <if test="messageType != null">message_type = #{messageType},</if>
                <if test="title != null">title = #{title},</if>
                <if test="content != null">content = #{content},</if>
                <if test="relatedBusinessType != null">related_business_type = #{relatedBusinessType},</if>
                <if test="relatedBusinessId != null">related_business_id = #{relatedBusinessId},</if>
                <if test="isRead != null">is_read = #{isRead},</if>
                <if test="readTime != null">read_time = #{readTime},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(SysMessage sysMessage);

    @Update("""
            UPDATE sys_message
            SET is_read = 1, read_time = NOW()
            WHERE id = #{id} AND receiver_user_id = #{receiverUserId}
            """)
    int markRead(@Param("id") Long id, @Param("receiverUserId") Long receiverUserId);

    @Select("""
            SELECT COUNT(1)
            FROM sys_message
            WHERE receiver_user_id = #{receiverUserId} AND is_read = 0
            """)
    Long countUnread(Long receiverUserId);
}
