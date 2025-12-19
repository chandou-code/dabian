# 校园失物招领系统 - 后端

基于SpringBoot + MyBatis Plus + MySQL的校园失物招领系统后端服务。

## 🚀 快速开始

### 1. 环境要求

- Java 11+
- MySQL 8.0+
- Maven 3.6+
- Redis (可选，用于缓存)

### 2. 数据库配置

1. 创建数据库：
```sql
mysql -u root -p < src/main/resources/sql/init.sql
```

2. 修改数据库连接配置（application.yml）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: your_password
```

### 3. 启动项目

```bash
# 编译项目
mvn clean compile

# 启动项目
mvn spring-boot:run
```

或者直接在IDE中运行 `LostFoundApplication.java` 主类。

### 4. 访问地址

- 应用地址：http://localhost:8080/api
- API文档：http://localhost:8080/api/doc.html
- 健康检查：http://localhost:8080/api/test/health

## 📁 项目结构

```
src/main/java/com/campus/lostfound/
├── LostFoundApplication.java          # 主启动类
├── common/                            # 通用类
│   ├── Result.java                    # 统一响应结果
│   ├── ResultCode.java                # 响应状态码枚举
│   └── constants/                     # 常量类
├── config/                            # 配置类
│   ├── CorsConfig.java               # 跨域配置
│   ├── MyBatisPlusConfig.java        # MyBatis Plus配置
│   ├── Knife4jConfig.java            # API文档配置
│   └── FileHandlerConfig.java        # 文件处理配置
├── controller/                        # 控制器层
├── entity/                           # 实体类
├── mapper/                           # 数据访问层
├── service/                           # 业务逻辑层
│   └── impl/                         # 业务逻辑实现
└── utils/                            # 工具类

src/main/resources/
├── mapper/                           # MyBatis XML映射文件
├── sql/                              # 数据库脚本
└── application.yml                    # 应用配置文件
```

## 🔐 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |
| 审核员 | reviewer | 123456 | 内容审核员 |
| 普通用户 | testuser | 123456 | 测试用户 |

## 📊 核心功能

### 1. 用户管理
- 用户注册、登录
- 用户信息管理
- 角色权限控制
- 用户状态管理

### 2. 失物招领
- 发布失物信息
- 发布招领信息
- 物品搜索浏览
- 智能匹配推荐

### 3. 审核管理
- 内容审核
- 审核记录
- 批量操作
- 审核统计

### 4. 数据统计
- 用户统计
- 物品统计
- 趋势分析
- 效率统计

### 5. 系统管理
- 系统配置
- 通知管理
- 文件上传
- 日志管理

## 🔧 API接口

### 系统测试
- `GET /api/test/health` - 健康检查
- `GET /api/test/info` - 系统信息

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/check-username/{username}` - 检查用户名
- `GET /api/auth/check-email/{email}` - 检查邮箱

### 物品管理接口
- `GET /api/items` - 获取物品列表
- `POST /api/items/lost` - 发布失物
- `POST /api/items/found` - 发布招领
- `GET /api/items/{id}` - 获取物品详情
- `GET /api/items/search` - 搜索物品
- `POST /api/items/{id}/found` - 标记物品已找到
- `POST /api/items/{id}/recovered` - 标记物品已领回
- `GET /api/items/activities` - 获取最近活动
- `GET /api/items/matches` - 获取推荐匹配
- `GET /api/items/stats/user` - 获取用户统计数据

### 审核管理接口
- `GET /api/review/pending` - 获取待审核列表
- `POST /api/review/approve/{id}` - 审核通过
- `POST /api/review/reject/{id}` - 审核驳回
- `POST /api/review/batch` - 批量审核
- `GET /api/review/stats` - 获取审核统计数据

### 管理员接口
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `POST /api/admin/users/{id}/reset-password` - 重置用户密码
- `POST /api/admin/users/batch` - 批量操作用户
- `GET /api/admin/stats` - 获取管理员统计数据
- `GET /api/admin/dashboard` - 获取仪表板统计
- `GET /api/admin/system/config` - 获取系统配置
- `PUT /api/admin/system/settings` - 更新系统设置

### 文件上传接口
- `POST /api/upload/image` - 上传单个图片
- `POST /api/upload/images` - 批量上传图片
- `DELETE /api/upload/image` - 删除文件

### 通知接口
- `GET /api/notifications` - 获取通知列表
- `GET /api/notifications/unread-count` - 获取未读通知数量
- `PUT /api/notifications/{id}/read` - 标记通知为已读
- `PUT /api/notifications/batch-read` - 批量标记为已读
- `PUT /api/notifications/all-read` - 标记所有通知为已读
- `DELETE /api/notifications/{id}` - 删除通知
- `DELETE /api/notifications/batch` - 批量删除通知
- `DELETE /api/notifications/all` - 清空所有通知
- `POST /api/notifications` - 创建通知

## 🔧 配置说明

### 应用配置 (application.yml)

```yaml
# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /api

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found
    username: root
    password: 123456

# JWT配置
app:
  jwt:
    secret: your-secret-key
    expiration: 604800000 # 7天

# 文件上传配置
  upload:
    path: /uploads/
    max-size: 10485760 # 10MB
```

### 阿里云OSS配置（可选）

```yaml
app:
  oss:
    enabled: true
    endpoint: https://oss-cn-beijing.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name
```

## 🧪 API测试

项目包含完整的API测试脚本 `test-api.http`，可以用IntelliJ IDEA或VSCode的REST Client插件直接测试。

### 快速测试步骤：

1. 启动项目后，访问健康检查：
   ```bash
   curl http://localhost:8080/api/test/health
   ```

2. 测试用户登录：
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"123456"}'
   ```

3. 测试获取物品列表：
   ```bash
   curl http://localhost:8080/api/items?current=1&pageSize=10
   ```

## 🚨 注意事项

1. **认证说明**：
   - 当前版本简化了JWT认证，Controller中的用户ID为硬编码
   - 生产环境需要实现完整的JWT Token解析和验证
   - Token验证应该通过Interceptor或Filter实现

2. **安全性**：
   - 生产环境请修改JWT密钥（application.yml中的app.jwt.secret）
   - 配置HTTPS访问
   - 修改默认数据库密码
   - 添加请求频率限制

3. **性能优化**：
   - 配置Redis缓存（application.yml中已配置Redis连接）
   - 使用数据库连接池（HikariCP已集成）
   - 配置CDN加速静态资源
   - 添加数据库查询缓存

4. **扩展功能**：
   - **AI图片识别**：需要配置AI服务商API（百度AI、腾讯AI等）
   - **短信通知**：需要配置短信服务商（阿里云短信、腾讯云短信等）
   - **邮件通知**：需要配置邮件服务器（SMTP设置）
   - **WebSocket**：实时通知推送

5. **生产部署**：
   - 配置生产环境数据库连接
   - 设置合理的JVM参数
   - 配置日志输出和轮转
   - 设置监控和告警

## 🐧 常见问题

### 1. 启动失败
- 检查MySQL服务是否启动
- 确认数据库连接配置正确
- 确认端口8080未被占用

### 2. 数据库连接失败
- 确认MySQL版本为8.0+
- 检查数据库URL中的时区设置
- 确认用户名密码正确

### 3. 文件上传失败
- 检查uploads目录权限
- 确认文件大小限制配置
- 检查磁盘空间

### 4. API调用失败
- 检查跨域配置
- 确认请求头Content-Type
- 查看后端日志获取详细错误信息

## 🤝 贡献

欢迎提交Issue和Pull Request来完善这个项目。

## 📄 许可证

MIT License