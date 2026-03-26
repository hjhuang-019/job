package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.Job;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobMapper {

    @Select("""
            SELECT id, enterprise_user_id, title, category, city, district, salary, work_mode,
                   education_requirement, experience_requirement, skill_requirements,
                   disability_support_type, accessibility_support_desc, job_description,
                   headcount, status, publish_time, expire_time, reviewed_by, reviewed_at,
                   created_at, updated_at
            FROM job
            WHERE id = #{id}
            """)
    Job selectById(Long id);

    @Select("""
            SELECT j.id, j.enterprise_user_id, j.title, j.category, j.city, j.district, j.salary, j.work_mode,
                   j.education_requirement, j.experience_requirement, j.skill_requirements,
                   j.disability_support_type, j.accessibility_support_desc, j.job_description,
                   j.headcount, j.status, j.publish_time, j.expire_time, j.reviewed_by, j.reviewed_at,
                   j.created_at, j.updated_at, ep.enterprise_name
            FROM job j
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            WHERE j.id = #{id} AND j.status = 'OPEN'
            """)
    Job selectPublishedById(Long id);

    @Select("""
            SELECT id, enterprise_user_id, title, category, city, district, salary, work_mode,
                   education_requirement, experience_requirement, skill_requirements,
                   disability_support_type, accessibility_support_desc, job_description,
                   headcount, status, publish_time, expire_time, reviewed_by, reviewed_at,
                   created_at, updated_at
            FROM job
            WHERE id = #{id} AND enterprise_user_id = #{enterpriseUserId}
            """)
    Job selectByIdAndEnterpriseUserId(@Param("id") Long id, @Param("enterpriseUserId") Long enterpriseUserId);

    @Select("""
            SELECT id, enterprise_user_id, title, category, city, district, salary, work_mode,
                   education_requirement, experience_requirement, skill_requirements,
                   disability_support_type, accessibility_support_desc, job_description,
                   headcount, status, publish_time, expire_time, reviewed_by, reviewed_at,
                   created_at, updated_at
            FROM job
            WHERE enterprise_user_id = #{enterpriseUserId}
            ORDER BY id DESC
            """)
    List<Job> selectByEnterpriseUserId(Long enterpriseUserId);

    @Select("""
            <script>
            SELECT j.id, j.enterprise_user_id, j.title, j.category, j.city, j.district, j.salary, j.work_mode,
                   j.education_requirement, j.experience_requirement, j.skill_requirements,
                   j.disability_support_type, j.accessibility_support_desc, j.job_description,
                   j.headcount, j.status, j.publish_time, j.expire_time, j.reviewed_by, j.reviewed_at,
                   j.created_at, j.updated_at, ep.enterprise_name
            FROM job j
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            WHERE j.status = 'OPEN'
              <if test="keyword != null and keyword != ''">
                AND (j.title LIKE CONCAT('%', #{keyword}, '%')
                     OR j.category LIKE CONCAT('%', #{keyword}, '%')
                     OR j.skill_requirements LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="city != null and city != ''">
                AND j.city = #{city}
              </if>
              <if test="workMode != null and workMode != ''">
                AND j.work_mode = #{workMode}
              </if>
              <if test="disabilitySupportType != null and disabilitySupportType != ''">
                AND j.disability_support_type LIKE CONCAT('%', #{disabilitySupportType}, '%')
              </if>
            ORDER BY j.publish_time DESC, j.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<Job> selectPublishedByPage(@Param("keyword") String keyword,
                                    @Param("city") String city,
                                    @Param("workMode") String workMode,
                                    @Param("disabilitySupportType") String disabilitySupportType,
                                    @Param("offset") Integer offset,
                                    @Param("pageSize") Integer pageSize);

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM job j
            WHERE j.status = 'OPEN'
              <if test="keyword != null and keyword != ''">
                AND (j.title LIKE CONCAT('%', #{keyword}, '%')
                     OR j.category LIKE CONCAT('%', #{keyword}, '%')
                     OR j.skill_requirements LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="city != null and city != ''">
                AND j.city = #{city}
              </if>
              <if test="workMode != null and workMode != ''">
                AND j.work_mode = #{workMode}
              </if>
              <if test="disabilitySupportType != null and disabilitySupportType != ''">
                AND j.disability_support_type LIKE CONCAT('%', #{disabilitySupportType}, '%')
              </if>
            </script>
            """)
    Long countPublished(@Param("keyword") String keyword,
                        @Param("city") String city,
                        @Param("workMode") String workMode,
                        @Param("disabilitySupportType") String disabilitySupportType);

    @Select("""
            SELECT j.id, j.enterprise_user_id, j.title, j.category, j.city, j.district, j.salary, j.work_mode,
                   j.education_requirement, j.experience_requirement, j.skill_requirements,
                   j.disability_support_type, j.accessibility_support_desc, j.job_description,
                   j.headcount, j.status, j.publish_time, j.expire_time, j.reviewed_by, j.reviewed_at,
                   j.created_at, j.updated_at, ep.enterprise_name
            FROM job j
            LEFT JOIN enterprise_profile ep ON ep.user_id = j.enterprise_user_id
            WHERE j.status = 'OPEN'
            ORDER BY j.publish_time DESC, j.id DESC
            """)
    List<Job> selectPublishedForRecommend();

    @Select("""
            SELECT id, enterprise_user_id, title, category, city, district, salary, work_mode,
                   education_requirement, experience_requirement, skill_requirements,
                   disability_support_type, accessibility_support_desc, job_description,
                   headcount, status, publish_time, expire_time, reviewed_by, reviewed_at,
                   created_at, updated_at
            FROM job
            ORDER BY id DESC
            """)
    List<Job> selectAll();

    @Select("SELECT COUNT(1) FROM job")
    Long countAll();

    @Insert("""
            INSERT INTO job (
                enterprise_user_id, title, category, city, salary, work_mode,
                education_requirement, experience_requirement, skill_requirements,
                disability_support_type, accessibility_support_desc, job_description,
                status, publish_time
            ) VALUES (
                #{enterpriseUserId}, #{title}, #{category}, #{city}, #{salary}, #{workMode},
                #{educationRequirement}, #{experienceRequirement}, #{skillRequirements},
                #{disabilitySupportType}, #{accessibilitySupportDesc}, #{jobDescription},
                #{status}, #{publishTime}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Job job);

    @Update("""
            <script>
            UPDATE job
            <set>
                <if test="title != null">title = #{title},</if>
                <if test="category != null">category = #{category},</if>
                <if test="city != null">city = #{city},</if>
                <if test="salary != null">salary = #{salary},</if>
                <if test="workMode != null">work_mode = #{workMode},</if>
                <if test="educationRequirement != null">education_requirement = #{educationRequirement},</if>
                <if test="experienceRequirement != null">experience_requirement = #{experienceRequirement},</if>
                <if test="skillRequirements != null">skill_requirements = #{skillRequirements},</if>
                <if test="disabilitySupportType != null">disability_support_type = #{disabilitySupportType},</if>
                <if test="accessibilitySupportDesc != null">accessibility_support_desc = #{accessibilitySupportDesc},</if>
                <if test="jobDescription != null">job_description = #{jobDescription},</if>
                <if test="status != null">status = #{status},</if>
                <if test="publishTime != null">publish_time = #{publishTime},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(Job job);

    @Delete("""
            DELETE FROM job WHERE id = #{id}
            """)
    int deleteById(Long id);
}
