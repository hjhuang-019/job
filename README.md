# 残疾人就业 Web 平台（毕业设计演示版）

本项目是一个前后端分离的就业服务平台，面向求职者、企业和管理员，包含认证审核、岗位管理、投递流程、消息通知、推荐岗位与基础无障碍支持功能。

## 1. 技术栈

- 后端：Spring Boot + MyBatis + MySQL + JWT + BCrypt
- 前端：Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios

## 2. 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 5.7/8.0

## 3. 数据库初始化（重点）

项目已提供初始化脚本：

- 表结构：`backend/src/main/resources/sql/schema.sql`
- 测试数据：`backend/src/main/resources/sql/data.sql`

### 3.1 手动导入方式（推荐用于答辩演示）

```sql
source D:/毕设/job/backend/src/main/resources/sql/schema.sql;
source D:/毕设/job/backend/src/main/resources/sql/data.sql;
```

### 3.2 数据库配置

默认配置见 `backend/src/main/resources/application.yml`：

- 数据库：`job_platform`
- 用户名：`root`
- 密码：`123456`
- 端口：`3306`

## 4. 启动步骤

### 4.1 启动后端

在 `backend` 目录执行：

```bash
# Windows
.\mvnw.cmd spring-boot:run

# 或
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

### 4.2 启动前端

在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 5. 默认测试账号

初始化 SQL 内置 3 个演示账号（密码统一为 `123456`）：

- 求职者：`seeker01`
- 企业：`enterprise01`
- 管理员：`admin01`

## 6. 初始化测试数据内容

`data.sql` 已覆盖完整业务场景，包含：

- 用户：1 个求职者、1 个企业、1 个管理员
- 求职者资料：含残障信息、技能、求职意向、证件路径（待审核）
- 企业资料：含企业信息、执照路径（待审核）
- 简历：3 份（1 份默认）
- 岗位：5 条（`OPEN/CLOSED/DRAFT` 多状态）
- 投递：3 条（`VIEWED/COMMUNICATING/REJECTED` 多状态）
- 消息：系统通知、审核通知、投递状态通知
- 审核日志、收藏、公告等演示数据

## 7. 演示效果优化说明

已对关键页面补充基础演示体验优化：

- 列表空状态提示（无数据时不空白）
- 接口失败错误提示（加载失败可感知）
- 操作成功提示（如删除、设默认、标记已读）
- 覆盖页面：岗位列表、简历列表、我的投递、岗位管理、消息中心

## 8. 推荐演示流程（毕业设计答辩）

建议按以下顺序演示，逻辑清晰、闭环完整：

1. **登录与角色切换**
   - 分别使用求职者、企业、管理员账号登录，展示角色化菜单与路由控制。
2. **企业发布岗位**
   - 企业端查看岗位管理数据，演示上架/下架、编辑、投递记录入口。
3. **求职者浏览与投递**
   - 求职者按关键字/城市筛选岗位，查看岗位详情，选择简历投递。
4. **企业处理投递**
   - 企业更新投递状态，填写备注。
5. **消息通知联动**
   - 求职者消息中心刷新后看到状态变更通知，并标记已读。
6. **管理员审核**
   - 管理员进入认证审核页，对求职者/企业执行通过或驳回。
7. **管理员统计**
   - 管理员统计页展示用户数、企业数、岗位数、投递数。
8. **无障碍能力展示**
   - 在顶部切换大字体、高对比度、焦点高亮，说明平台可访问性设计。

## 9. 常见问题

- 后端启动失败：先检查 MySQL 和 `application.yml` 配置。
- 前端请求失败：确认后端已启动且 `vite.config.js` 代理正确。
- 数据异常：重新执行 `schema.sql + data.sql` 重置环境。