# 校园失物招领系统数据库设计

## 数据库结构

### 数据库名称
`campus_lost_found`

### 字符集
`utf8mb4` (支持完整的UTF-8字符集，包括emoji)

## 表结构说明

### 1. users - 用户表
存储系统所有用户信息，包括学生、管理员和审核员。

**主要字段：**
- `id`: 用户ID（主键）
- `username`: 用户名（唯一）
- `password`: 加密后的密码
- `real_name`: 真实姓名
- `email`: 邮箱地址
- `phone`: 手机号
- `role`: 用户角色（user/admin/reviewer）
- `avatar`: 头像URL
- `college`: 学院
- `grade`: 年级
- `status`: 账户状态（1-启用，0-禁用）

### 2. items - 失物招领物品表
存储所有的失物和招领信息。

**主要字段：**
- `id`: 物品ID（主键）
- `item_name`: 物品名称
- `category`: 物品类别
- `description`: 详细描述
- `images`: 物品图片URL数组（JSON格式）
- `contact`: 联系方式
- `location`: 丢失/发现地点
- `event_time`: 丢失/发现时间
- `type`: 类型（lost-失物，found-招领）
- `status`: 状态（pending/approved/rejected/claimed）
- `submitter_id`: 发布者ID
- `reviewer_id`: 审核者ID
- `matched_item_id`: 匹配的物品ID

### 3. notifications - 通知表
存储用户通知信息。

**主要字段：**
- `id`: 通知ID（主键）
- `user_id`: 接收者ID
- `title`: 通知标题
- `content`: 通知内容
- `type`: 通知类型（approval/rejection/match/recovery/system）
- `related_item_id`: 相关物品ID
- `is_read`: 是否已读（0-未读，1-已读）

### 4. matches - 匹配记录表
存储失物与招领的匹配记录。

**主要字段：**
- `id`: 匹配记录ID（主键）
- `lost_item_id`: 失物ID
- `found_item_id`: 招领ID
- `match_score`: 匹配分数（0-100）
- `similarity_details`: 相似度详情（JSON格式）
- `status`: 匹配状态（pending/confirmed/rejected）
- `confirmed_by`: 确认者ID

### 5. reviews - 审核记录表
存储物品审核记录。

**主要字段：**
- `id`: 审核记录ID（主键）
- `item_id`: 物品ID
- `reviewer_id`: 审核者ID
- `action`: 审核动作（approved/rejected）
- `reason`: 审核原因
- `review_time`: 审核时间

### 6. system_configs - 系统配置表
存储系统配置参数。

**主要字段：**
- `id`: 配置ID（主键）
- `config_key`: 配置键（唯一）
- `config_value`: 配置值
- `description`: 配置描述
- `config_type`: 配置类型（string/number/boolean/json）
- `is_public`: 是否公开（0-私有，1-公开）

### 7. announcements - 公告表
存储系统公告信息。

**主要字段：**
- `id`: 公告ID（主键）
- `title`: 公告标题
- `content`: 公告内容
- `status`: 状态（active/inactive）
- `created_by`: 创建者ID

### 8. file_uploads - 文件上传记录表
记录所有文件上传信息。

**主要字段：**
- `id`: 文件记录ID（主键）
- `original_name`: 原始文件名
- `file_name`: 存储文件名
- `file_path`: 文件路径
- `file_size`: 文件大小（字节）
- `upload_user_id`: 上传者ID
- `related_item_id`: 相关物品ID

## 索引设计

### 主要索引
- **用户表**: username, email, role, status
- **物品表**: type, status, category, submitter_id, event_time, location
- **通知表**: user_id, is_read, type
- **匹配表**: lost_item_id, found_item_id, status, match_score
- **审核表**: item_id, reviewer_id, action

### 全文索引
- **物品表**: item_name, description, location（支持全文搜索）

## 外键约束

```sql
items.submitter_id -> users.id
items.reviewer_id -> users.id  
items.matched_item_id -> items.id
notifications.user_id -> users.id
notifications.related_item_id -> items.id
matches.lost_item_id -> items.id
matches.found_item_id -> items.id
matches.created_by -> users.id
matches.confirmed_by -> users.id
reviews.item_id -> items.id
reviews.reviewer_id -> users.id
announcements.created_by -> users.id
file_uploads.upload_user_id -> users.id
file_uploads.related_item_id -> items.id
```

## 数据类型说明

### JSON字段
- `items.images`: 存储图片URL数组，格式如 `["url1", "url2"]`
- `matches.similarity_details`: 存储相似度详情，格式如 `{"category": 100, "location": 90}`

### 枚举字段
- `users.role`: ENUM('user', 'admin', 'reviewer')
- `items.type`: ENUM('lost', 'found')
- `items.status`: ENUM('pending', 'approved', 'rejected', 'claimed')
- `notifications.type`: ENUM('approval', 'rejection', 'match', 'recovery', 'system')
- `matches.status`: ENUM('pending', 'confirmed', 'rejected')
- `reviews.action`: ENUM('approved', 'rejected')
- `announcements.status`: ENUM('active', 'inactive')
- `system_configs.config_type`: ENUM('string', 'number', 'boolean', 'json')

## 默认账号信息

测试数据中包含以下默认账号，密码统一为 `123456`（已加密存储）：

### 管理员账号
- **用户名**: admin
- **密码**: 123456
- **角色**: 管理员

### 审核员账号
- **用户名**: reviewer1
- **密码**: 123456
- **角色**: 审核员

- **用户名**: reviewer2  
- **密码**: 123456
- **角色**: 审核员

### 普通用户账号
- **用户名**: user1 (张三 - 计算机学院2022级)
- **用户名**: user2 (李四 - 信息工程学院2021级)
- **用户名**: user3 (王五 - 电子工程学院2022级)
- **用户名**: user4 (赵六 - 管理学院2020级)
- **用户名**: user5 (孙七 - 外国语学院2021级)

## 使用说明

### 1. 创建数据库
```sql
-- 执行建表SQL
mysql -u root -p < campus_lost_found.sql
```

### 2. 插入测试数据
```sql
-- 执行测试数据SQL
mysql -u root -p campus_lost_found < test_data.sql
```

### 3. 数据库连接配置
在 `application.yml` 中配置数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: 123456
```

## 数据统计（测试数据）

### 用户统计
- 总用户数: 8人
- 管理员: 1人
- 审核员: 2人  
- 普通用户: 5人

### 物品统计
- 总物品数: 12件
- 失物: 6件
- 招领: 6件
- 已审核: 10件
- 待审核: 2件
- 已认领: 2件

### 其他数据
- 匹配记录: 4条
- 审核记录: 10条
- 通知消息: 8条
- 系统公告: 3条
- 文件记录: 13条

## 注意事项

1. **密码加密**: 所有用户密码都使用BCrypt加密存储
2. **逻辑删除**: 重要表使用 `deleted` 字段进行逻辑删除
3. **时间戳**: 使用 `created_at` 和 `updated_at` 记录创建和更新时间
4. **JSON支持**: 图片URL数组和相似度详情使用JSON格式存储
5. **全文搜索**: 物品名称、描述和地点支持全文搜索功能