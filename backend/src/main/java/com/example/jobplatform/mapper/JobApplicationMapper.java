package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.JobApplication;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobApplicationMapper {

    @Select("""
            SELECT a.id, a.job_id, a.job_seeker_user_id, a.resume_id, a.status, a.apply_time,
                   a.feedback_content, a.handled_time, a.created_at, a.updated_at,
                   j.title AS job_title,
                   ep.enterprise_name,
                   COALESCE(su.real_name, su.username) AS job_seeker_name,
                   r.resume_name
            FROM job_application a
            LEFT JOIN job j ON j.id = a.job_id
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            LEFT JOIN sys_user su ON su.id = a.job_seeker_user_id
            LEFT JOIN resume r ON r.id = a.resume_id
            WHERE a.id = #{id}
            """)
    JobApplication selectById(Long id);

    @Select("""
            SELECT a.id, a.job_id, a.job_seeker_user_id, a.resume_id, a.status, a.apply_time,
                   a.feedback_content, a.handled_time, a.created_at, a.updated_at,
                   j.title AS job_title,
                   ep.enterprise_name,
                   COALESCE(su.real_name, su.username) AS job_seeker_name,
                   r.resume_name
            FROM job_application a
            LEFT JOIN job j ON j.id = a.job_id
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            LEFT JOIN sys_user su ON su.id = a.job_seeker_user_id
            LEFT JOIN resume r ON r.id = a.resume_id
            WHERE a.job_id = #{jobId}
            ORDER BY a.apply_time DESC, a.id DESC
            """)
    List<JobApplication> selectByJobId(Long jobId);

    @Select("""
            SELECT a.id, a.job_id, a.job_seeker_user_id, a.resume_id, a.status, a.apply_time,
                   a.feedback_content, a.handled_time, a.created_at, a.updated_at,
                   j.title AS job_title,
                   ep.enterprise_name,
                   COALESCE(su.real_name, su.username) AS job_seeker_name,
                   r.resume_name
            FROM job_application a
            LEFT JOIN job j ON j.id = a.job_id
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            LEFT JOIN sys_user su ON su.id = a.job_seeker_user_id
            LEFT JOIN resume r ON r.id = a.resume_id
            WHERE a.job_seeker_user_id = #{jobSeekerUserId}
            ORDER BY a.apply_time DESC, a.id DESC
            """)
    List<JobApplication> selectByJobSeekerUserId(Long jobSeekerUserId);

    @Select("""
            SELECT id, job_id, job_seeker_user_id, resume_id, status, apply_time,
                   feedback_content, handled_time, created_at, updated_at
            FROM job_application
            ORDER BY id DESC
            """)
    List<JobApplication> selectAll();

    @Select("SELECT COUNT(1) FROM job_application")
    Long countAll();

    @Insert("""
            INSERT INTO job_application (
                job_id, job_seeker_user_id, resume_id, status, apply_time, feedback_content, handled_time
            ) VALUES (
                #{jobId}, #{jobSeekerUserId}, #{resumeId}, #{status}, #{applyTime}, #{feedbackContent}, #{handledTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(JobApplication jobApplication);

    @Update("""
            <script>
            UPDATE job_application
            <set>
                <if test="status != null">status = #{status},</if>
                <if test="resumeId != null">resume_id = #{resumeId},</if>
                <if test="feedbackContent != null">feedback_content = #{feedbackContent},</if>
                <if test="handledTime != null">handled_time = #{handledTime},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(JobApplication jobApplication);

    @Delete("""
            DELETE FROM job_application WHERE id = #{id}
            """)
    int deleteById(Long id);

    @Select("""
            SELECT id, job_id, job_seeker_user_id, resume_id, status, apply_time,
                   feedback_content, handled_time, created_at, updated_at
            FROM job_application
            WHERE job_id = #{jobId} AND job_seeker_user_id = #{jobSeekerUserId}
            LIMIT 1
            """)
    JobApplication selectByJobIdAndJobSeekerUserId(@Param("jobId") Long jobId, @Param("jobSeekerUserId") Long jobSeekerUserId);
}
