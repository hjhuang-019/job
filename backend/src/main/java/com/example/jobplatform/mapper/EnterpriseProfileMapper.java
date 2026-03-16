package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.EnterpriseProfile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EnterpriseProfileMapper {

    @Select("""
            SELECT id, user_id, enterprise_name, unified_social_credit_code, industry, scale_type,
                   contact_person, contact_phone, address, description, accessibility_support_desc,
                   license_path, verify_status, verified_by, verified_at, created_at, updated_at
            FROM enterprise_profile
            WHERE id = #{id}
            """)
    EnterpriseProfile selectById(Long id);

    @Select("""
            SELECT id, user_id, enterprise_name, unified_social_credit_code, industry, scale_type,
                   contact_person, contact_phone, address, description, accessibility_support_desc,
                   license_path, verify_status, verified_by, verified_at, created_at, updated_at
            FROM enterprise_profile
            WHERE user_id = #{userId}
            """)
    EnterpriseProfile selectByUserId(Long userId);

    @Select("""
            SELECT id, user_id, enterprise_name, unified_social_credit_code, industry, scale_type,
                   contact_person, contact_phone, address, description, accessibility_support_desc,
                   license_path, verify_status, verified_by, verified_at, created_at, updated_at
            FROM enterprise_profile
            ORDER BY id DESC
            """)
    List<EnterpriseProfile> selectAll();

    @Insert("""
            INSERT INTO enterprise_profile (
                user_id, enterprise_name, unified_social_credit_code, industry, scale_type,
                contact_person, contact_phone, address, description, accessibility_support_desc,
                license_path, verify_status, verified_by, verified_at
            ) VALUES (
                #{userId}, #{enterpriseName}, #{unifiedSocialCreditCode}, #{industry}, #{scaleType},
                #{contactPerson}, #{contactPhone}, #{address}, #{description}, #{accessibilitySupportDesc},
                #{licensePath}, #{verifyStatus}, #{verifiedBy}, #{verifiedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EnterpriseProfile enterpriseProfile);

    @Update("""
            <script>
            UPDATE enterprise_profile
            <set>
                <if test="userId != null">user_id = #{userId},</if>
                <if test="enterpriseName != null">enterprise_name = #{enterpriseName},</if>
                <if test="unifiedSocialCreditCode != null">unified_social_credit_code = #{unifiedSocialCreditCode},</if>
                <if test="industry != null">industry = #{industry},</if>
                <if test="scaleType != null">scale_type = #{scaleType},</if>
                <if test="contactPerson != null">contact_person = #{contactPerson},</if>
                <if test="contactPhone != null">contact_phone = #{contactPhone},</if>
                <if test="address != null">address = #{address},</if>
                <if test="description != null">description = #{description},</if>
                <if test="accessibilitySupportDesc != null">accessibility_support_desc = #{accessibilitySupportDesc},</if>
                <if test="licensePath != null">license_path = #{licensePath},</if>
                <if test="verifyStatus != null">verify_status = #{verifyStatus},</if>
                <if test="verifiedBy != null">verified_by = #{verifiedBy},</if>
                <if test="verifiedAt != null">verified_at = #{verifiedAt},</if>
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateById(EnterpriseProfile enterpriseProfile);

    @Delete("""
            DELETE FROM enterprise_profile WHERE id = #{id}
            """)
    int deleteById(Long id);
}
