package com.example.jobplatform.service;

import com.example.jobplatform.dto.EnterpriseProfileUpdateRequest;
import com.example.jobplatform.vo.EnterpriseProfileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EnterpriseService {

    EnterpriseProfileVO getProfile();

    EnterpriseProfileVO updateProfile(EnterpriseProfileUpdateRequest request);

    Map<String, String> uploadLicense(MultipartFile file);

    EnterpriseProfileVO submitVerify();
}
