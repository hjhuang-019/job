package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.SysAnnouncement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysAnnouncementMapper {

    SysAnnouncement selectById(Long id);

    List<SysAnnouncement> selectAll();

    int insert(SysAnnouncement sysAnnouncement);

    int updateById(SysAnnouncement sysAnnouncement);

    int deleteById(Long id);
}
