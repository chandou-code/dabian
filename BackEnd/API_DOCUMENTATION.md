# 校园失物招领系统API文档

## 1. 概述

### 1.1 系统简介
校园失物招领系统是一个用于管理校园内失物和招领信息的平台，支持用户发布、搜索、浏览失物招领信息。

### 1.2 API基础信息
- **基础URL**：`http://localhost:18080/api`
- **请求格式**：JSON
- **响应格式**：JSON
- **认证方式**：JWT Token（Bearer Token）

### 1.3 响应结构
所有API响应遵循统一格式：
```json
{
  "code": 200,  // 状态码，200表示成功
  "message": "操作成功",  // 状态消息
  "data": {}  // 响应数据
}
```

## 2. 认证相关API

### 2.1 用户注册
- **请求方法**：POST
- **API路径**：`/auth/register`
- **请求体**：
  ```json
  {
    "username": "testuser_123456",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000",
    "realName": "测试用户",
    "gender": 1,  // 1:男, 0:女
    "college": "计算机学院",
    "grade": "2022级",
    "major": "软件工程"
  }
  ```
- **响应数据**：
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 25,
      "username": "testuser_123456",
      "realName": "测试用户",
      "email": "test@example.com",
      "phone": "13800138000",
      "gender": 1,
      "college": "计算机学院",
      "grade": "2022级",
      "major": "软件工程",
      "role": "user",
      "createdAt": "2025-12-18T02:16:20"
    }
  }
  ```

### 2.2 用户登录
- **请求方法**：POST
- **API路径**：`/auth/login`
- **请求体**：
  ```json
  {
    "username": "testuser_123456",
    "password": "123456"
  }
  ```
- **响应数据**：与注册成功的响应数据格式相同

### 2.3 用户登出
- **请求方法**：POST
- **API路径**：`/auth/logout`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **响应数据**：
  ```json
  {
    "message": "登出成功"
  }
  ```

## 3. 物品管理API

### 3.1 发布失物信息
- **请求方法**：POST
- **API路径**：`/items/lost-items`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  {
    "title": "丢失白色手机",
    "itemName": "iPhone 14",
    "category": "电子设备",
    "itemTime": "2025-12-18T02:16:21",
    "location": "图书馆",
    "locationDetail": "三楼自习室",
    "description": "白色iPhone 14，有透明手机壳，背面有贴纸",
    "contact": "13800138000"
  }
  ```
- **响应数据**：
  ```json
  {
    "id": 23,
    "title": "丢失白色手机",
    "itemName": "iPhone 14",
    "category": "电子设备",
    "itemTime": "2025-12-18T02:16:21",
    "location": "图书馆",
    "locationDetail": "三楼自习室",
    "description": "白色iPhone 14，有透明手机壳，背面有贴纸",
    "contact": "13800138000",
    "submitterId": 25,
    "status": "pending",
    "createdAt": "2025-12-18T02:16:21"
  }
  ```

### 3.2 发布招领信息
- **请求方法**：POST
- **API路径**：`/items/found-items`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  {
    "title": "拾到黑色钱包",
    "itemName": "黑色钱包",
    "category": "证件类",
    "itemTime": "2025-12-18T02:16:21",
    "location": "食堂",
    "locationDetail": "二楼餐桌",
    "description": "黑色皮质钱包，内有校园卡和银行卡",
    "contact": "13800138000",
    "pickupLocation": "学生处"
  }
  ```
- **响应数据**：与发布失物信息的响应格式相同

### 3.3 获取物品列表
- **请求方法**：GET
- **API路径**：`/items/lost-items` 或 `/items/found-items`
- **查询参数**：
  - `current`：当前页码（默认1）
  - `pageSize`：每页条数（默认10）
- **响应数据**：
  ```json
  {
    "list": [
      {
        "id": 23,
        "title": "丢失白色手机",
        "itemName": "iPhone 14",
        "category": "电子设备",
        "location": "图书馆",
        "status": "pending",
        "createdAt": "2025-12-18T02:16:21"
      }
    ],
    "total": 10,
    "current": 1,
    "pageSize": 10
  }
  ```

### 3.4 搜索物品
- **请求方法**：GET
- **API路径**：`/items/search`
- **查询参数**：
  - `q`：搜索关键词
  - `type`：物品类型（"lost"、"found"或"all"，默认"all"）
  - `current`：当前页码（默认1）
  - `pageSize`：每页条数（默认10）
- **响应数据**：
  ```json
  {
    "results": [
      {
        "id": 23,
        "title": "丢失白色手机",
        "itemName": "iPhone 14",
        "category": "电子设备",
        "location": "图书馆",
        "status": "pending",
        "createdAt": "2025-12-18T02:16:21"
      }
    ],
    "total": 0,
    "current": 1,
    "pageSize": 10
  }
  ```

### 3.5 获取物品详情
- **请求方法**：GET
- **API路径**：
  - 失物详情：`/items/lost-item/{itemId}`
  - 招领详情：`/items/found-item/{itemId}`
- **路径参数**：
  - `itemId`：物品ID
- **响应数据**：
  ```json
  {
    "id": 23,
    "title": "丢失白色手机",
    "itemName": "iPhone 14",
    "category": "电子设备",
    "itemTime": "2025-12-18T02:16:21",
    "location": "图书馆",
    "locationDetail": "三楼自习室",
    "description": "白色iPhone 14，有透明手机壳，背面有贴纸",
    "contact": "13800138000",
    "submitterId": 25,
    "submitterName": "测试用户",
    "status": "pending",
    "createdAt": "2025-12-18T02:16:21",
    "updatedAt": "2025-12-18T02:16:21",
    "viewCount": 1
  }
  ```

### 3.6 更新物品信息
- **请求方法**：PUT
- **API路径**：`/items/items/{itemId}`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **路径参数**：
  - `itemId`：物品ID
- **请求体**：
  ```json
  {
    "title": "更新后的标题",
    "description": "更新后的描述信息 - 这是一个测试更新"
  }
  ```
- **响应数据**：
  ```json
  {
    "id": 23,
    "title": "更新后的标题",
    "itemName": "iPhone 14",
    "description": "更新后的描述信息 - 这是一个测试更新"
  }
  ```

### 3.7 删除物品信息
- **请求方法**：DELETE
- **API路径**：`/items/items/{itemId}`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **路径参数**：
  - `itemId`：物品ID
- **响应数据**：
  ```json
  {
    "message": "删除成功"
  }
  ```

## 4. 用户统计API

### 4.1 获取用户统计信息
- **请求方法**：GET
- **API路径**：`/items/stats/user`
- **认证要求**：需要JWT Token
- **请求头**：`Authorization: Bearer <token>`
- **响应数据**：
  ```json
  {
    "recovered": 0,
    "pending": 2,
    "recentActivities": [
      {
        "icon": "✅",
        "description": "黑色皮质钱包，内有校园卡和银行卡",
        "id": 24,
        "time": "0分钟前",
        "title": "发布了招领信息",
        "status": "pending"
      }
    ],
    "totalLost": 1,
    "totalFound": 1,
    "recommendedMatches": [
      {
        "image": "/uploads/items/backpack1.jpg",
        "score": 89,
        "itemName": "双肩背包",
        "description": "黑色双肩背包，内有笔记本电脑和书籍",
        "location": "教学楼",
        "id": 1
      }
    ]
  }
  ```

## 5. 系统API

### 5.1 系统健康检查
- **请求方法**：GET
- **API路径**：`/test/health`
- **响应数据**：
  ```json
  {
    "message": "校园失物招领系统运行正常"
  }
  ```

## 6. API使用示例

### 6.1 使用Python requests库调用API示例
```python
import requests
import json

base_url = "http://localhost:18080/api"

# 1. 用户注册
register_data = {
    "username": "testuser_123",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000",
    "realName": "测试用户",
    "gender": 1,
    "college": "计算机学院",
    "grade": "2022级",
    "major": "软件工程"
}

response = requests.post(f"{base_url}/auth/register", json=register_data)
if response.status_code == 200:
    result = response.json()
    token = result['data']['token']
    print(f"注册成功，token: {token}")

# 2. 发布失物信息（需要认证）
headers = {
    'Content-Type': 'application/json',
    'Authorization': f'Bearer {token}'
}

lost_item_data = {
    "title": "丢失白色手机",
    "itemName": "iPhone 14",
    "category": "电子设备",
    "itemTime": "2025-12-18T02:16:21",
    "location": "图书馆",
    "locationDetail": "三楼自习室",
    "description": "白色iPhone 14，有透明手机壳，背面有贴纸",
    "contact": "13800138000"
}

response = requests.post(f"{base_url}/items/lost-items", json=lost_item_data, headers=headers)
if response.status_code == 200:
    result = response.json()
    print(f"发布失物成功，物品ID: {result['data']['id']}")
```

## 7. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 拒绝访问，权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 8. 注意事项

1. 所有需要认证的API都需要在请求头中添加`Authorization: Bearer <token>`
2. 日期时间格式统一为ISO 8601格式：`YYYY-MM-DDTHH:mm:ss`
3. 文件上传API尚未在测试中体现，具体实现请参考后端代码
4. 物品状态分为：pending（待审核）、approved（已审核）、found（已找到）、recovered（已领回）
5. 所有API都支持分页参数`current`和`pageSize`
