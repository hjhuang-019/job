package com.example.jobplatform.service;

import com.example.jobplatform.dto.ResumeSaveRequest;
import com.example.jobplatform.vo.ResumeVO;

import java.util.List;

public interface ResumeService {

    ResumeVO createResume(ResumeSaveRequest request);

    List<ResumeVO> listResumes();

    ResumeVO getResume(Long id);

    ResumeVO updateResume(Long id, ResumeSaveRequest request);

    void deleteResume(Long id);
}
