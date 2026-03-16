package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysAuditLogMapper {

    SysAuditLog selectById(Long id);

    List<SysAuditLog> selectByBusinessType(String businessType);

    List<SysAuditLog> selectAll();

    int insert(SysAuditLog sysAuditLog);

    int updateById(SysAuditLog sysAuditLog);

    int deleteById(Long id);
}
