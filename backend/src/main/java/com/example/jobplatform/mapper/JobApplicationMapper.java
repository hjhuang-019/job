package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobApplicationMapper {

    JobApplication selectById(Long id);

    List<JobApplication> selectByJobId(Long jobId);

    List<JobApplication> selectByJobSeekerUserId(Long jobSeekerUserId);

    List<JobApplication> selectAll();

    int insert(JobApplication jobApplication);

    int updateById(JobApplication jobApplication);

    int deleteById(Long id);
}
