package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.Resume;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeMapper {

    @Select("""
            SELECT id, user_id, resume_name, resume_type, file_path, content_text,
                   education_experience, work_experience, project_experience, skill_summary,
                   disability_type, disability_level,
                   is_default, status, created_at, updated_at
            FROM resume
            WHERE id = #{id} AND status = 1
            """)
    Resume selectById(Long id);

    @Select("""
            SELECT id, user_id, resume_name, resume_type, file_path, content_text,
                   education_experience, work_experience, project_experience, skill_summary,
                   disability_type, disability_level,
                   is_default, status, created_at, updated_at
            FROM resume
            WHERE user_id = #{userId} AND status = 1
            ORDER BY is_default DESC, id DESC
            """)
    List<Resume> selectByUserId(Long userId);

    @Select("""
            SELECT id, user_id, resume_name, resume_type, file_path, content_text,
                   education_experience, work_experience, project_experience, skill_summary,
                   disability_type, disability_level,
                   is_default, status, created_at, updated_at
            FROM resume
            WHERE status = 1
            ORDER BY id DESC
            """)
    List<Resume> selectAll();

    @Insert("""
            INSERT INTO resume (
                user_id, resume_name, resume_type, file_path, content_text,
                education_experience, work_experience, project_experience, skill_summary,
                disability_type, disability_level,
                is_default, status
            ) VALUES (
                #{userId}, #{resumeName}, #{resumeType}, #{filePath}, #{contentText},
                #{educationExperience}, #{workExperience}, #{projectExperience}, #{skillSummary},
                #{disabilityType}, #{disabilityLevel},
                #{isDefault}, #{status}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Update("""
            <script>
            UPDATE resume
            <set>
                <if test="resumeName != null">resume_name = #{resumeName},</if>
                <if test="resumeType != null">resume_type = #{resumeType},</if>
                <if test="filePath != null">file_path = #{filePath},</if>
                <if test="contentText != null">content_text = #{contentText},</if>
                <if test="educationExperience != null">education_experience = #{educationExperience},</if>
                <if test="workExperience != null">work_experience = #{workExperience},</if>
                <if test="projectExperience != null">project_experience = #{projectExperience},</if>
                <if test="skillSummary != null">skill_summary = #{skillSummary},</if>
                disability_type = #{disabilityType,jdbcType=VARCHAR},
                disability_level = #{disabilityLevel,jdbcType=VARCHAR},
                <if test="isDefault != null">is_default = #{isDefault},</if>
                <if test="status != null">status = #{status},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(Resume resume);

    @Delete("""
            DELETE FROM resume WHERE id = #{id}
            """)
    int deleteById(Long id);

    @Update("""
            UPDATE resume
            SET is_default = 0
            WHERE user_id = #{userId} AND status = 1
            """)
    int clearDefaultByUserId(Long userId);

    @Select("""
            SELECT id, user_id, resume_name, resume_type, file_path, content_text,
                   education_experience, work_experience, project_experience, skill_summary,
                   disability_type, disability_level,
                   is_default, status, created_at, updated_at
            FROM resume
            WHERE id = #{id} AND user_id = #{userId} AND status = 1
            """)
    Resume selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT id, user_id, resume_name, resume_type, file_path, content_text,
                   education_experience, work_experience, project_experience, skill_summary,
                   disability_type, disability_level,
                   is_default, status, created_at, updated_at
            FROM resume
            WHERE user_id = #{userId} AND status = 1
            ORDER BY id DESC
            LIMIT 1
            """)
    Resume selectLatestByUserId(Long userId);
}
