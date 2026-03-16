package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.Job;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobMapper {

    Job selectById(Long id);

    List<Job> selectByEnterpriseUserId(Long enterpriseUserId);

    List<Job> selectAll();

    int insert(Job job);

    int updateById(Job job);

    int deleteById(Long id);
}
