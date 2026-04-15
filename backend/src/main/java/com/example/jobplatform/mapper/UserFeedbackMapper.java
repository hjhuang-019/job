package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.UserFeedback;
import com.example.jobplatform.vo.AdminUserFeedbackRowVO;
import com.example.jobplatform.vo.UserFeedbackMineVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserFeedbackMapper {

    @Insert("""
            INSERT INTO sys_user_feedback (sender_user_id, category, title, content, status)
            VALUES (#{senderUserId}, #{category}, #{title}, #{content}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserFeedback feedback);

    @Select("""
            SELECT COUNT(1) FROM sys_user_feedback
            WHERE sender_user_id = #{senderUserId} AND status = 'PENDING'
            """)
    long countPendingBySenderId(@Param("senderUserId") Long senderUserId);

    @Select("""
            SELECT id, category, title, status, created_at, admin_remark, handled_at
            FROM sys_user_feedback
            WHERE sender_user_id = #{senderUserId}
            ORDER BY id DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<UserFeedbackMineVO> selectMinePage(@Param("senderUserId") Long senderUserId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    @Select("""
            SELECT COUNT(1) FROM sys_user_feedback WHERE sender_user_id = #{senderUserId}
            """)
    long countMine(@Param("senderUserId") Long senderUserId);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM sys_user_feedback f
            JOIN sys_user u ON u.id = f.sender_user_id
            WHERE 1=1
            <if test="status != null and status != ''">
              AND f.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (f.title LIKE CONCAT('%', #{keyword}, '%')
                OR f.content LIKE CONCAT('%', #{keyword}, '%')
                OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countForAdmin(@Param("status") String status, @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT f.id, f.sender_user_id, u.username AS sender_username, u.role AS sender_role,
                   f.category, f.title, f.content, f.status, f.created_at,
                   f.admin_user_id, f.admin_remark, f.handled_at
            FROM sys_user_feedback f
            JOIN sys_user u ON u.id = f.sender_user_id
            WHERE 1=1
            <if test="status != null and status != ''">
              AND f.status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (f.title LIKE CONCAT('%', #{keyword}, '%')
                OR f.content LIKE CONCAT('%', #{keyword}, '%')
                OR u.username LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY f.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AdminUserFeedbackRowVO> selectPageForAdmin(@Param("status") String status,
                                                    @Param("keyword") String keyword,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

    @Select("""
            SELECT id, sender_user_id, category, title, content, status, admin_user_id, admin_remark, handled_at, created_at
            FROM sys_user_feedback
            WHERE id = #{id}
            """)
    UserFeedback selectById(Long id);

    @Update("""
            UPDATE sys_user_feedback
            SET status = 'RESOLVED',
                admin_user_id = #{adminUserId},
                admin_remark = #{adminRemark},
                handled_at = #{handledAt}
            WHERE id = #{id} AND status = 'PENDING'
            """)
    int updateResolve(@Param("id") Long id,
                      @Param("adminUserId") Long adminUserId,
                      @Param("adminRemark") String adminRemark,
                      @Param("handledAt") java.time.LocalDateTime handledAt);
}
