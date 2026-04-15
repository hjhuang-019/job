package com.example.jobplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 启动时移除 job_application 上仅按 (job_id, job_seeker_user_id) 的唯一约束，
 * 以便同一求职者用不同简历多次投递同一岗位，或在拒绝后用同一简历再投。
 * 与 resources/sql/migration_20260412_job_application_multi_apply.sql 等价；索引已不存在时忽略错误。
 */
@Component
@Order(0)
public class JobApplicationMultiApplyMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationMultiApplyMigrationRunner.class);

    private final DataSource dataSource;

    public JobApplicationMultiApplyMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE job_application DROP INDEX uk_job_application_unique");
            log.info("已移除 job_application.uk_job_application_unique，支持同岗多简历/拒绝后再投。");
        } catch (SQLException e) {
            if (isMissingIndexError(e)) {
                log.debug("uk_job_application_unique 不存在，跳过 DROP：{}", e.getMessage());
            } else {
                log.warn("未能自动 DROP uk_job_application_unique，可手动执行 migration_20260412_job_application_multi_apply.sql：{}", e.getMessage());
            }
        }

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE job_application ADD INDEX idx_job_application_job_seeker (job_id, job_seeker_user_id)");
            log.debug("已确保 idx_job_application_job_seeker 存在。");
        } catch (SQLException e) {
            if (isDuplicateIndexName(e)) {
                log.debug("idx_job_application_job_seeker 已存在，跳过 ADD INDEX。");
            } else {
                log.warn("未能自动 ADD idx_job_application_job_seeker：{}", e.getMessage());
            }
        }
    }

    private static boolean isMissingIndexError(SQLException e) {
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        // MySQL 1091 ER_CANT_DROP_FIELD_OR_KEY
        if ("42000".equals(e.getSQLState()) && e.getErrorCode() == 1091) {
            return true;
        }
        String lower = m.toLowerCase();
        return lower.contains("can't drop") && lower.contains("check that column/key exists")
                || lower.contains("unknown key");
    }

    private static boolean isDuplicateIndexName(SQLException e) {
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        // MySQL 1061 ER_DUP_KEYNAME
        if ("42000".equals(e.getSQLState()) && e.getErrorCode() == 1061) {
            return true;
        }
        String lower = m.toLowerCase();
        return lower.contains("duplicate key name");
    }
}
