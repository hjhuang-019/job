package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.JobSeekerProfile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobSeekerProfileMapper {

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            WHERE id = #{id}
            """)
    JobSeekerProfile selectById(Long id);

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            WHERE user_id = #{userId}
            """)
    JobSeekerProfile selectByUserId(Long userId);

    @Select("""
            SELECT id, user_id, gender, birth_date, disability_type, disability_level, education, skills,
                   expected_city, expected_salary, introduction, certificate_path, verify_status,
                   verified_by, verified_at, created_at, updated_at
            FROM job_seeker_profile
            ORDER BY id DESC
            """)
    List<JobSeekerProfile> selectAll();

    @Insert("""
            INSERT INTO job_seeker_profile (
                user_id, gender, birth_date, disability_type, disability_level, education, skills,
                expected_city, expected_salary, introduction, certificate_path, verify_status,
                verified_by, verified_at
            ) VALUES (
                #{userId}, #{gender}, #{birthDate}, #{disabilityType}, #{disabilityLevel}, #{education}, #{skills},
                #{expectedCity}, #{expectedSalary}, #{introduction}, #{certificatePath}, #{verifyStatus},
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
}
