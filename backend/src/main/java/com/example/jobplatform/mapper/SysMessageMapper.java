package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysMessageMapper {

    SysMessage selectById(Long id);

    List<SysMessage> selectByReceiverUserId(Long receiverUserId);

    List<SysMessage> selectAll();

    int insert(SysMessage sysMessage);

    int updateById(SysMessage sysMessage);

    int deleteById(Long id);
}
