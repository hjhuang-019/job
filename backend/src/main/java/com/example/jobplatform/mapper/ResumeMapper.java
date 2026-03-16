package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ResumeMapper {

    Resume selectById(Long id);

    List<Resume> selectByUserId(Long userId);

    List<Resume> selectAll();

    int insert(Resume resume);

    int updateById(Resume resume);

    int deleteById(Long id);
}
