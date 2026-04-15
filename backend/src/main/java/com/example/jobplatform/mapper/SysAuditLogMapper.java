package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysAuditLog;
import com.example.jobplatform.vo.AdminAuditLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysAuditLogMapper {

    @Insert("""
            INSERT INTO sys_audit_log (business_type, business_id, audit_status, audit_comment, operator_user_id, operated_at)
            VALUES (#{businessType}, #{businessId}, #{auditStatus}, #{auditComment}, #{operatorUserId}, #{operatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysAuditLog sysAuditLog);

    @Select("""
            SELECT l.id, l.business_type, l.business_id, l.audit_status, l.audit_comment,
                   l.operator_user_id, l.operated_at,
                   u.username AS operator_username
            FROM sys_audit_log l
            LEFT JOIN sys_user u ON u.id = l.operator_user_id
            WHERE l.business_type = #{businessType} AND l.business_id = #{businessId}
            ORDER BY l.operated_at DESC, l.id DESC
            """)
    List<AdminAuditLogVO> selectListForAdminByBusiness(@Param("businessType") String businessType,
                                                        @Param("businessId") Long businessId);
}
