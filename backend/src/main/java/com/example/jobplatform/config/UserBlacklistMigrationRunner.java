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
 * 启动时为 sys_user 补齐黑名单相关列（与 migration_20260415_sys_user_blacklist.sql 等价）；列已存在则跳过。
 */
@Component
@Order(1)
public class UserBlacklistMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserBlacklistMigrationRunner.class);

    private final DataSource dataSource;

    public UserBlacklistMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        addColumnIgnoreDuplicate(
                "ALTER TABLE sys_user ADD COLUMN blacklisted TINYINT NOT NULL DEFAULT 0 COMMENT '是否黑名单：0否 1是' AFTER status");
        addColumnIgnoreDuplicate(
                "ALTER TABLE sys_user ADD COLUMN blacklisted_at DATETIME DEFAULT NULL COMMENT '列入黑名单时间' AFTER blacklisted");
        addColumnIgnoreDuplicate(
                "ALTER TABLE sys_user ADD COLUMN blacklisted_by BIGINT DEFAULT NULL COMMENT '操作管理员用户ID' AFTER blacklisted_at");
        addColumnIgnoreDuplicate(
                "ALTER TABLE sys_user ADD COLUMN blacklist_reason VARCHAR(500) DEFAULT NULL COMMENT '拉黑原因' AFTER blacklisted_by");
    }

    private void addColumnIgnoreDuplicate(String ddl) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
            log.info("已执行黑名单字段迁移：{}", ddl.substring(0, Math.min(80, ddl.length())));
        } catch (SQLException e) {
            if (isDuplicateColumnError(e)) {
                log.debug("黑名单字段已存在，跳过：{}", e.getMessage());
            } else {
                log.warn("黑名单字段迁移失败，可手动执行 migration_20260415_sys_user_blacklist.sql：{}", e.getMessage());
            }
        }
    }

    private static boolean isDuplicateColumnError(SQLException e) {
        if (e.getErrorCode() == 1060) {
            return true;
        }
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        String lower = m.toLowerCase();
        return lower.contains("duplicate column name");
    }
}
