package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobSeekerProfileUpdateRequest;
import com.example.jobplatform.vo.JobSeekerProfileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface JobSeekerService {

    JobSeekerProfileVO getProfile();

    JobSeekerProfileVO updateProfile(JobSeekerProfileUpdateRequest request);

    Map<String, String> uploadCertificate(MultipartFile file);

    JobSeekerProfileVO submitVerify();
}
