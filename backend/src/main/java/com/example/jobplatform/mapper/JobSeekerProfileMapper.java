package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobSeekerProfileMapper {

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, expected_job, accept_remote, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            WHERE id = #{id}
            """)
    JobSeekerProfile selectById(Long id);

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, expected_job, accept_remote, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            WHERE user_id = #{userId}
            """)
    JobSeekerProfile selectByUserId(Long userId);

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, expected_job, accept_remote, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            ORDER BY id DESC
            """)
    List<JobSeekerProfile> selectAll();

    @Select("""
            SELECT p.id, p.user_id, u.username, COALESCE(u.real_name, u.username) AS real_name, u.phone,
                   p.disability_type, p.disability_level, p.certificate_path, p.verify_status, p.created_at
            FROM job_seeker_profile p
            JOIN sys_user u ON u.id = p.user_id
            WHERE u.role = 'JOB_SEEKER'
              AND p.verify_status = 'PENDING'
              AND p.certificate_path IS NOT NULL
              AND p.certificate_path <> ''
            ORDER BY p.id DESC
            """)
    List<AdminJobSeekerPendingVO> selectPendingForAdmin();

    @Insert("""
            INSERT INTO job_seeker_profile (
                user_id, gender, birth_date, disability_type, disability_level, education, skills,
                expected_city, expected_salary, expected_job, accept_remote, introduction, certificate_path, verify_status,
                verified_by, verified_at
            ) VALUES (
                #{userId}, #{gender}, #{birthDate}, #{disabilityType}, #{disabilityLevel}, #{education}, #{skills},
                #{expectedCity}, #{expectedSalary}, #{expectedJob}, #{acceptRemote}, #{introduction}, #{certificatePath}, #{verifyStatus},
                #{verifiedBy}, #{verifiedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobSeekerProfile jobSeekerProfile);

    @Update("""
            <script>
            UPDATE job_seeker_profile
            <set>
                <if test="userId != null">user_id = #{userId},</if>
                <if test="gender != null">gender = #{gender},</if>
                <if test="birthDate != null">birth_date = #{birthDate},</if>
                <if test="disabilityType != null">disability_type = #{disabilityType},</if>
                <if test="disabilityLevel != null">disability_level = #{disabilityLevel},</if>
                <if test="education != null">education = #{education},</if>
                <if test="skills != null">skills = #{skills},</if>
                <if test="expectedCity != null">expected_city = #{expectedCity},</if>
                <if test="expectedSalary != null">expected_salary = #{expectedSalary},</if>
                <if test="expectedJob != null">expected_job = #{expectedJob},</if>
                <if test="acceptRemote != null">accept_remote = #{acceptRemote},</if>
                <if test="introduction != null">introduction = #{introduction},</if>
                <if test="certificatePath != null">certificate_path = #{certificatePath},</if>
                <if test="verifyStatus != null">verify_status = #{verifyStatus},</if>
                <if test="verifiedBy != null">verified_by = #{verifiedBy},</if>
                <if test="verifiedAt != null">verified_at = #{verifiedAt},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(JobSeekerProfile jobSeekerProfile);

    @Delete("""
            DELETE FROM job_seeker_profile WHERE id = #{id}
            """)
    int deleteById(Long id);

    @Update("""
            UPDATE job_seeker_profile
            SET verify_status = #{verifyStatus},
                verified_by = NULL,
                verified_at = NULL
            WHERE id = #{id}
            """)
    int updateVerifyStatusAsPending(@Param("id") Long id, @Param("verifyStatus") String verifyStatus);

    @Update("""
            UPDATE job_seeker_profile
            SET verify_status = #{verifyStatus},
                verified_by = #{verifiedBy},
                verified_at = #{verifiedAt}
            WHERE id = #{id}
            """)
    int updateVerifyByAdmin(@Param("id") Long id,
                            @Param("verifyStatus") String verifyStatus,
                            @Param("verifiedBy") Long verifiedBy,
                            @Param("verifiedAt") java.time.LocalDateTime verifiedAt);
}
