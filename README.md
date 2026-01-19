# 在线答题系统 (Online Quiz System)

一个功能完善的在线答题学习平台，支持 Web 端和微信小程序，提供题库管理、在线练习、考试模式、错题本、收藏夹、学习统计等功能。

## 项目架构

```
online-quiz-system/
├── backend/          # 后端服务 (Spring Boot)
├── frontend/         # Web前端 (Vue 3)
├── miniprogram/      # 微信小程序 (uni-app)
└── uploads/          # 文件上传目录
```

## 技术栈

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.0.2 | 基础框架 |
| Spring Security | - | 安全认证 |
| MyBatis Plus | 3.5.4.1 | ORM框架 |
| MySQL | 8.x | 数据库 |
| Redis | - | 缓存/验证码存储 |
| JWT | 0.11.5 | 身份认证 |
| Apache POI | 5.2.4 | Excel文件处理 |
| SpringDoc OpenAPI | 2.2.0 | API文档 |
| Hutool | 5.8.23 | 工具类库 |
| Lombok | - | 代码简化 |

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.8 | 前端框架 |
| TypeScript | 5.3 | 开发语言 |
| Vite | 5.0.6 | 构建工具 |
| Element Plus | 2.4.4 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.2 | HTTP客户端 |

### 小程序技术
| 技术 | 说明 |
|------|------|
| uni-app | 跨平台框架 |
| 微信小程序 | 目标平台 |

## 功能特性

### 用户功能
- **用户注册/登录**：支持用户名密码登录，邮箱验证码注册
- **个人信息管理**：修改昵称、头像、密码
- **学习统计**：查看个人答题统计、正确率、学习进度

### 题库管理
- **题库创建**：支持手动创建空题库
- **题目导入**：支持 Excel (.xlsx/.xls) 格式批量导入
- **题库编辑**：编辑题库描述、状态、排序
- **题库删除**：删除题库及其所有题目

### 答题功能
- **练习模式**：顺序答题，即时显示答案解析，支持断点续答
- **考试模式**：模拟考试，统一交卷批改
- **断点续答**：自动保存答题进度，下次继续答题
- **多种题型**：支持单选题、多选题、判断题、简答题

### 学习辅助
- **错题本**：自动记录错题，支持错题复习
- **收藏夹**：收藏重点题目，便于复习
- **答案解析**：每道题提供详细解析

### 统计分析
- **个人统计**：答题总数、正确率、学习时长
- **学习进度**：本周学习天数、连续学习天数
- **排行榜**：用户成绩排行

### 管理功能
- **权限管理**：管理员可管理用户权限
- **会话管理**：管理员可查看所有答题记录
- **用户管理**：启用/禁用用户账户

## 数据库设计

### 数据表结构

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| question_banks | 题库表 |
| questions | 题目表 |
| quiz_sessions | 答题会话表 |
| user_answers | 用户答题记录表 |
| wrong_questions | 错题表 |
| favorites | 收藏表 |

### ER图概述

```
users (用户)
  ├── question_banks (题库) - 创建者
  ├── questions (题目) - 创建者
  ├── quiz_sessions (答题会话) - 一对多
  ├── user_answers (答题记录) - 一对多
  ├── wrong_questions (错题) - 一对多
  └── favorites (收藏) - 一对多

question_banks (题库)
  └── questions (题目) - 一对多
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 1. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE quiz_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入数据表
-- 执行 backend/src/main/resources/quiz_system_v2.sql
```

### 2. 后端启动

```bash
cd backend

# 修改配置文件
# 编辑 src/main/resources/application-dev.yml
# 配置数据库连接、Redis连接、邮箱服务等

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/online-quiz-system-backend-1.0.0.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:9090`

API文档地址：`http://localhost:9090/api/swagger-ui.html`

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端默认运行在 `http://localhost:5173`

### 4. 小程序运行

```bash
cd miniprogram

# 使用 HBuilderX 打开项目
# 或使用命令行

# 安装依赖
npm install

# 运行到微信开发者工具
npm run dev:mp-weixin
```

## 配置说明

### 环境变量配置

敏感信息通过环境变量配置，避免提交到代码仓库：

| 环境变量 | 说明 | 示例 |
|----------|------|------|
| `MAIL_USERNAME` | 邮箱账号 | `your_email@qq.com` |
| `MAIL_PASSWORD` | 邮箱授权码 | `your_auth_code` |
| `JWT_SECRET` | JWT密钥（至少32字符） | `your-secret-key-at-least-32-chars` |

**Linux/macOS 设置环境变量：**
```bash
export MAIL_USERNAME=your_email@qq.com
export MAIL_PASSWORD=your_auth_code
export JWT_SECRET=your-secret-key-at-least-32-chars
```

**Windows 设置环境变量：**
```cmd
set MAIL_USERNAME=your_email@qq.com
set MAIL_PASSWORD=your_auth_code
set JWT_SECRET=your-secret-key-at-least-32-chars
```

### 后端配置 (application.yml)

```yaml
server:
  port: 9090
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quiz_system
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

  mail:
    host: smtp.qq.com
    username: ${MAIL_USERNAME:your_email@qq.com}
    password: ${MAIL_PASSWORD:your_auth_code}

jwt:
  secret: ${JWT_SECRET:your-jwt-secret-key}
  expiration: 172800  # 2天
```

### 生产环境配置

1. 复制示例配置文件：
```bash
cp backend/src/main/resources/application-prod.yml.example backend/src/main/resources/application-prod.yml
```

2. 编辑 `application-prod.yml` 填入实际的生产环境配置

3. 启动时指定生产环境：
```bash
java -jar target/online-quiz-system-backend-1.0.0.jar --spring.profiles.active=prod
```

**注意**：`application-prod.yml` 已添加到 `.gitignore`，不会被提交到代码仓库。

### 前端配置

在 `frontend/src/utils/apiClient.ts` 中配置后端接口地址：

```typescript
const apiClient = axios.create({
  baseURL: 'http://localhost:9090/api',
  timeout: 10000
})
```

### 小程序配置

在 `miniprogram/utils/store.js` 中配置后端接口地址。

## Excel题目导入格式

支持 `.xlsx` 和 `.xls` 格式，表格结构如下：

| 试题类型 | 题目 | 答案 | 解析 | 选项1 | 选项2 | 选项3 | 选项4 |
|---------|------|------|------|-------|-------|-------|-------|
| 单选题 | 题目内容 | A | 答案解析 | 选项A | 选项B | 选项C | 选项D |
| 多选题 | 题目内容 | A,B,C | 答案解析 | 选项A | 选项B | 选项C | 选项D |
| 判断题 | 题目内容 | 正确 | 答案解析 | | | | |
| 简答题 | 题目内容 | 参考答案 | 答案解析 | | | | |

**说明：**
- **试题类型**：支持中文（单选题/多选题/判断题/简答题）或英文（single/multiple/judge/essay）
- **答案格式**：
  - 单选题：填选项序号（1/2/3...）或字母（A/B/C...）
  - 多选题：用逗号分隔，如 "1,2,3" 或 "A,B,C"
  - 判断题：填"正确"或"错误"
- **选项**：从第5列开始，选择题至少需要2个选项

## API接口

### 主要接口模块

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证管理 | /auth | 注册、登录、用户信息 |
| 题库管理 | /banks | 题库CRUD、导入 |
| 题目管理 | /questions | 题目CRUD |
| 答题会话 | /sessions | 开始答题、提交答案 |
| 错题管理 | /wrong-questions | 错题列表、标记掌握 |
| 收藏管理 | /favorites | 添加/取消收藏 |
| 统计分析 | /statistics | 系统统计、用户统计 |
| 文件上传 | /files | 头像、题库文件上传 |
| 权限管理 | /permissions | 用户权限管理（管理员） |
| 管理功能 | /admin | 用户管理、会话管理（管理员） |

### 接口文档

启动后端服务后访问 Swagger UI：
- 地址：`http://localhost:9090/api/swagger-ui.html`
- API文档：`http://localhost:9090/api/v3/api-docs`

## 项目截图

### Web端功能
- 首页：系统介绍、快速入口
- 题库列表：显示所有题库及学习进度
- 练习模式：顺序答题、即时反馈
- 考试模式：模拟考试环境
- 错题本：错题复习
- 统计分析：学习数据可视化

### 小程序功能
- 首页：题库选择、快速开始
- 答题界面：支持练习和考试模式
- 我的：个人信息、错题本、收藏

## 默认账户

系统初始化后会创建默认管理员账户：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

**注意**：请在生产环境中及时修改默认密码。

## 部署说明

### Docker部署（推荐）

```bash
# 构建后端镜像
cd backend
docker build -t quiz-backend .

# 构建前端镜像
cd frontend
docker build -t quiz-frontend .

# 使用 docker-compose 启动
docker-compose up -d
```

### 传统部署

1. **后端**：将打包好的 jar 文件部署到服务器
2. **前端**：将 `dist` 目录部署到 Nginx
3. **配置 Nginx 反向代理**

Nginx 配置示例：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/quiz-frontend;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 开发指南

### 目录结构

#### 后端结构
```
backend/src/main/java/com/quiz/
├── annotation/       # 自定义注解
├── aspect/          # AOP切面
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── exception/       # 异常处理
├── handler/         # 类型处理器
├── mapper/          # MyBatis Mapper
├── service/         # 服务层
│   └── impl/        # 服务实现
├── strategy/        # 策略模式
│   └── impl/        # 策略实现
└── utils/           # 工具类
```

#### 前端结构
```
frontend/src/
├── api/             # API接口
├── components/      # 公共组件
├── router/          # 路由配置
├── store/           # Pinia状态管理
├── utils/           # 工具函数
└── views/           # 页面组件
    ├── admin/       # 管理页面
    ├── auth/        # 认证页面
    ├── bank/        # 题库页面
    ├── exam/        # 考试页面
    ├── practice/    # 练习页面
    ├── question/    # 题目页面
    ├── result/      # 结果页面
    └── user/        # 用户页面
```

### 代码规范

- 后端遵循阿里巴巴Java开发规范
- 前端遵循 Vue 3 组合式 API 风格
- 使用 ESLint + Prettier 进行代码格式化

## 常见问题

### 1. 数据库连接失败
- 检查 MySQL 服务是否启动
- 检查数据库用户名密码是否正确
- 检查数据库是否已创建

### 2. Redis连接失败
- 检查 Redis 服务是否启动
- 检查 Redis 端口和密码配置

### 3. 邮件发送失败
- 检查邮箱 SMTP 配置
- QQ邮箱需要使用授权码而非登录密码

### 4. 文件上传失败
- 检查 uploads 目录是否存在且有写入权限
- 检查文件大小是否超过限制（默认10MB）

### 5. 小程序无法连接后端
- 检查后端服务是否正常运行
- 小程序需要配置合法的服务器域名（HTTPS）

## 许可证

本项目采用 MIT 许可证。

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 联系方式

如有问题或建议，请提交 Issue。
