package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.vo.AdminEnterpriseUserRowVO;
import com.example.jobplatform.vo.AdminJobSeekerUserRowVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("""
            SELECT id, username, password, role, status, blacklisted, blacklisted_at, blacklisted_by, blacklist_reason,
                   real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE id = #{id}
            """)
    SysUser selectById(Long id);

    @Select("""
            SELECT id, username, password, role, status, blacklisted, blacklisted_at, blacklisted_by, blacklist_reason,
                   real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE username = #{username}
            """)
    SysUser selectByUsername(String username);

    @Select("""
            SELECT id, username, password, role, status, blacklisted, blacklisted_at, blacklisted_by, blacklist_reason,
                   real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE phone = #{phone}
            """)
    SysUser selectByPhone(String phone);

    @Select("""
            SELECT id, username, password, role, status, blacklisted, blacklisted_at, blacklisted_by, blacklist_reason,
                   real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            ORDER BY id DESC
            """)
    List<SysUser> selectAll();

    @Select("""
            SELECT COUNT(1)
            FROM sys_user
            WHERE role = #{role}
            """)
    Long countByRole(@Param("role") String role);

    @Select("""
            SELECT id FROM sys_user WHERE role = #{role}
            """)
    List<Long> selectIdsByRole(@Param("role") String role);

    @Insert("""
            INSERT INTO sys_user (username, password, role, status, real_name, phone, email, last_login_time)
            VALUES (#{username}, #{password}, #{role}, #{status}, #{realName}, #{phone}, #{email}, #{lastLoginTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser sysUser);

    @Update("""
            <script>
            UPDATE sys_user
            <set>
                <if test="username != null">username = #{username},</if>
                <if test="password != null">password = #{password},</if>
                <if test="role != null">role = #{role},</if>
                <if test="status != null">status = #{status},</if>
                <if test="realName != null">real_name = #{realName},</if>
                <if test="phone != null">phone = #{phone},</if>
                <if test="email != null">email = #{email},</if>
                <if test="lastLoginTime != null">last_login_time = #{lastLoginTime},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(SysUser sysUser);

    @Delete("""
            DELETE FROM sys_user WHERE id = #{id}
            """)
    int deleteById(Long id);

    @Update("""
            UPDATE sys_user
            SET blacklisted = 1,
                blacklisted_at = #{blacklistedAt},
                blacklisted_by = #{blacklistedBy},
                blacklist_reason = #{blacklistReason}
            WHERE id = #{id}
            """)
    int markBlacklisted(@Param("id") Long id,
                        @Param("blacklistedAt") java.time.LocalDateTime blacklistedAt,
                        @Param("blacklistedBy") Long blacklistedBy,
                        @Param("blacklistReason") String blacklistReason);

    @Update("""
            UPDATE sys_user
            SET blacklisted = 0,
                blacklisted_at = NULL,
                blacklisted_by = NULL,
                blacklist_reason = NULL
            WHERE id = #{id}
            """)
    int clearBlacklist(@Param("id") Long id);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM sys_user u
            WHERE u.role = 'JOB_SEEKER'
            <if test="keyword != null and keyword != ''">
              AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.real_name, '') LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.phone, '') LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countJobSeekerUsersForAdmin(@Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT u.id AS user_id, u.username, u.real_name, u.phone, u.email, u.status AS user_status, u.blacklisted AS blacklisted,
                   u.created_at AS user_created_at,
                   p.id AS profile_id, p.disability_type, p.disability_level, p.certificate_path, p.verify_status, p.verified_at
            FROM sys_user u
            LEFT JOIN job_seeker_profile p ON p.user_id = u.id
            WHERE u.role = 'JOB_SEEKER'
            <if test="keyword != null and keyword != ''">
              AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.real_name, '') LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.phone, '') LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY u.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AdminJobSeekerUserRowVO> selectJobSeekerUsersPageForAdmin(@Param("keyword") String keyword,
                                                                   @Param("offset") int offset,
                                                                   @Param("limit") int limit);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM sys_user u
            WHERE u.role = 'ENTERPRISE'
            <if test="keyword != null and keyword != ''">
              AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.real_name, '') LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.phone, '') LIKE CONCAT('%', #{keyword}, '%')
                OR EXISTS (
                  SELECT 1 FROM enterprise_profile ep
                  WHERE ep.user_id = u.id
                    AND IFNULL(ep.enterprise_name, '') LIKE CONCAT('%', #{keyword}, '%')
                ))
            </if>
            </script>
            """)
    long countEnterpriseUsersForAdmin(@Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT u.id AS user_id, u.username, u.real_name, u.phone, u.email, u.status AS user_status, u.blacklisted AS blacklisted,
                   u.created_at AS user_created_at,
                   p.id AS profile_id, p.enterprise_name, p.contact_person, p.contact_phone, p.license_path, p.verify_status, p.verified_at
            FROM sys_user u
            LEFT JOIN enterprise_profile p ON p.user_id = u.id
            WHERE u.role = 'ENTERPRISE'
            <if test="keyword != null and keyword != ''">
              AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.real_name, '') LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(u.phone, '') LIKE CONCAT('%', #{keyword}, '%')
                OR IFNULL(p.enterprise_name, '') LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY u.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AdminEnterpriseUserRowVO> selectEnterpriseUsersPageForAdmin(@Param("keyword") String keyword,
                                                                       @Param("offset") int offset,
                                                                       @Param("limit") int limit);
}
