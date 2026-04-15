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
 * 启动时确保 sys_user_feedback 表存在（与 migration_20260415_sys_user_feedback.sql 等价）。
 */
@Component
@Order(2)
public class UserFeedbackTableMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserFeedbackTableMigrationRunner.class);

    private static final String CREATE_SQL = """
            CREATE TABLE IF NOT EXISTS sys_user_feedback (
              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户反馈工单ID',
              sender_user_id BIGINT NOT NULL COMMENT '提交人用户ID',
              category VARCHAR(30) NOT NULL DEFAULT 'GENERAL' COMMENT '类型：GENERAL一般咨询/BLACKLIST_APPEAL黑名单申诉',
              title VARCHAR(150) NOT NULL COMMENT '标题',
              content TEXT NOT NULL COMMENT '正文',
              status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RESOLVED',
              admin_user_id BIGINT DEFAULT NULL COMMENT '处理管理员用户ID',
              admin_remark VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
              handled_at DATETIME DEFAULT NULL COMMENT '处理时间',
              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
              KEY idx_user_feedback_sender (sender_user_id),
              KEY idx_user_feedback_status_created (status, created_at),
              CONSTRAINT fk_user_feedback_sender FOREIGN KEY (sender_user_id) REFERENCES sys_user (id),
              CONSTRAINT fk_user_feedback_admin FOREIGN KEY (admin_user_id) REFERENCES sys_user (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈工单（黑名单申诉等）'
            """;

    private final DataSource dataSource;

    public UserFeedbackTableMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(CREATE_SQL);
            log.info("已确保 sys_user_feedback 表存在。");
        } catch (SQLException e) {
            log.warn("创建 sys_user_feedback 表失败，可手动执行 migration_20260415_sys_user_feedback.sql：{}", e.getMessage());
        }
    }
}
