package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("""
            SELECT id, username, password, role, status, real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE id = #{id}
            """)
    SysUser selectById(Long id);

    @Select("""
            SELECT id, username, password, role, status, real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE username = #{username}
            """)
    SysUser selectByUsername(String username);

    @Select("""
            SELECT id, username, password, role, status, real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            WHERE phone = #{phone}
            """)
    SysUser selectByPhone(String phone);

    @Select("""
            SELECT id, username, password, role, status, real_name, phone, email, last_login_time, created_at, updated_at
            FROM sys_user
            ORDER BY id DESC
            """)
    List<SysUser> selectAll();

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
}
