# 失物招领系统CRUD技术文档

## 目录
- [系统架构](#系统架构)
- [数据表设计](#数据表设计)
- [后端CRUD流程](#后端crud流程)
- [前端CRUD操作](#前端crud操作)
- [API接口规范](#api接口规范)
- [数据流向图](#数据流向图)

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端(Vue)   │───▶│  后端(Spring)  │───▶│  数据库(MySQL)  │
│                │    │                │    │                │
│ - 页面展示       │    │ - 接收请求      │    │ - 数据存储       │
│ - 用户交互       │    │ - 业务逻辑      │    │ - 数据持久化     │
│ - 数据发送       │    │ - 数据验证      │    │ - 事务管理       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 数据表设计

### 1. items表 (失物招领物品表)

```sql
CREATE TABLE items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,              -- 物品ID
    user_id BIGINT,                                  -- 用户ID
    item_name VARCHAR(255) NOT NULL,                  -- 物品名称
    category VARCHAR(100),                           -- 物品分类
    description TEXT,                                -- 物品描述
    images TEXT,                                    -- 图片URL(JSON格式)
    contact VARCHAR(255),                            -- 联系方式
    location VARCHAR(255),                            -- 丢失/发现地点
    event_time DATE,                                 -- 事件时间(丢失时间/发现时间)
    type ENUM('lost','found') NOT NULL DEFAULT 'lost', -- 类型: lost-失物, found-招领
    status ENUM('pending','approved','rejected','claimed') DEFAULT 'pending',
    submitter_id BIGINT NOT NULL,                     -- 提交者ID
    reviewer_id BIGINT,                               -- 审核者ID
    review_reason TEXT,                                -- 审核原因
    matched_item_id BIGINT,                           -- 匹配的物品ID
    recovered_at DATETIME,                             -- 认领/找回时间
    matched_time DATETIME,                             -- 匹配时间
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,         -- 创建时间
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    deleted TINYINT DEFAULT 0,                        -- 逻辑删除标记
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (submitter_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (matched_item_id) REFERENCES items(id)
);
```

### 2. users表 (用户表)

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,              -- 用户ID
    username VARCHAR(50) UNIQUE NOT NULL,              -- 用户名
    password VARCHAR(255) NOT NULL,                   -- 加密密码
    real_name VARCHAR(100),                           -- 真实姓名
    email VARCHAR(100) UNIQUE,                        -- 邮箱
    phone VARCHAR(20),                               -- 手机号
    role ENUM('user','reviewer','admin') DEFAULT 'user', -- 角色
    avatar VARCHAR(255),                              -- 头像URL
    college VARCHAR(100),                             -- 学院
    grade VARCHAR(50),                               -- 年级
    status TINYINT DEFAULT 1,                         -- 状态: 0-禁用, 1-启用
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,         -- 创建时间
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,                        -- 逻辑删除标记
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

## 后端CRUD流程

### Controller层 (ItemController.java)

```java
@RestController
@RequestMapping("/items")
@CrossOrigin
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;
    
    // 1. 查询列表 - 分页获取失物/招领列表
    @GetMapping("/lost-items")
    public Result<Map<String, Object>> getItems(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long submitterId) {
        
        try {
            log.info("获取失物列表请求: current={}, pageSize={}", current, pageSize);
            var page = itemService.getItemPage(current, pageSize, "lost", category, status, keyword, location, submitterId);
            
            Map<String, Object> result = Map.of(
                "total", page.getTotal(),
                "list", page.getRecords().stream().map(item -> {
                    Map<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("itemName", item.getItemName());
                    itemMap.put("category", item.getCategory());
                    
                    // 确保时间字段不为null，转换为适合前端的格式
                    if (item.getEventTime() != null) {
                        itemMap.put("lostTime", item.getEventTime() + "T00:00:00");
                    } else {
                        itemMap.put("lostTime", null);
                    }
                    
                    // 确保位置字段不为null
                    itemMap.put("lostLocation", item.getLocation() != null ? item.getLocation() : "未知地点");
                    
                    itemMap.put("description", item.getDescription());
                    itemMap.put("images", getImagesArray(item.getImages()));
                    itemMap.put("status", item.getStatus());
                    itemMap.put("type", item.getType()); // 添加type字段
                    itemMap.put("userId", item.getSubmitterId());
                    itemMap.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
                    
                    // 手动加载用户名
                    try {
                        User user = userService.getById(item.getSubmitterId());
                        itemMap.put("userName", user != null ? user.getRealName() : "匿名用户");
                    } catch (Exception e) {
                        log.warn("获取用户名失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                        itemMap.put("userName", "匿名用户");
                    }
                    
                    return itemMap;
                }).toList()
            );
            
            log.info("获取失物列表成功: total={}", page.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取失物列表失败", e);
            return Result.error("获取失物列表失败：" + e.getMessage());
        }
    }
    
    // 2. 创建 - 发布失物
    @PostMapping("/lost-items")
    public Result<Item> publishLostItem(@RequestBody Map<String, Object> requestData, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("发布失物信息请求: {}", requestData.get("itemName"));
            
            Long userId = extractUserIdFromToken(authorization);
            if (userId == null) {
                return Result.error("用户未登录或token无效");
            }
            
            // 创建Item对象并手动映射字段
            Item item = new Item();
            item.setItemName((String) requestData.get("itemName"));
            item.setCategory((String) requestData.get("category"));
            item.setDescription((String) requestData.get("description"));
            item.setContact((String) requestData.get("contact"));
            item.setImages((String) requestData.get("images"));
            item.setType((String) requestData.get("type"));
            
            // 映射时间字段
            String lostTimeStr = (String) requestData.get("lostTime");
            if (lostTimeStr != null && !lostTimeStr.isEmpty()) {
                try {
                    item.setEventTime(LocalDate.parse(lostTimeStr));
                } catch (Exception e) {
                    log.warn("解析丢失时间失败: {}", lostTimeStr);
                }
            }
            
            // 映射位置字段
            String location = (String) requestData.get("lostLocation");
            if (location != null && !location.isEmpty()) {
                item.setLocation(location);
            }
            
            Item savedItem = itemService.publishLostItem(item, userId);
            if (savedItem != null) {
                log.info("发布失物信息成功: id={}, userId={}", savedItem.getId(), userId);
                return Result.success("发布成功", savedItem);
            } else {
                return Result.error("发布失败");
            }
        } catch (Exception e) {
            log.error("发布失物信息失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    // 3. 查询详情 - 获取单个物品详情
    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getItemDetailByFrontend(@PathVariable Long id) {
        try {
            log.info("获取物品详情请求: id={}", id);
            
            Item item = itemService.getItemDetail(id);
            if (item == null) {
                log.warn("物品不存在: id={}", id);
                return Result.error("物品不存在");
            }
            
            // 构建返回数据，确保包含前端所需的所有字段
            Map<String, Object> itemDetail = new java.util.HashMap<>();
            itemDetail.put("id", item.getId());
            itemDetail.put("itemName", item.getItemName());
            itemDetail.put("category", item.getCategory());
            itemDetail.put("type", item.getType());
            itemDetail.put("description", item.getDescription());
            itemDetail.put("status", item.getStatus());
            itemDetail.put("submitterId", item.getSubmitterId());
            itemDetail.put("created_at", item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
            
            // 根据类型设置时间和地点字段
            if ("lost".equals(item.getType())) {
                itemDetail.put("eventTime", item.getEventTime() != null ? item.getEventTime().toString() : null);
                itemDetail.put("location", item.getLocation() != null ? item.getLocation() : "未知地点");
            } else {
                itemDetail.put("eventTime", item.getEventTime() != null ? item.getEventTime().toString() : null);
                itemDetail.put("location", item.getLocation() != null ? item.getLocation() : "未知地点");
            }
            
            // 处理图片数组
            itemDetail.put("images", getImagesArray(item.getImages()));
            
            // 获取用户信息
            try {
                User user = userService.getById(item.getSubmitterId());
                if (user != null) {
                    itemDetail.put("userName", user.getRealName() != null ? user.getRealName() : user.getUsername());
                    itemDetail.put("userPhone", user.getPhone());
                    itemDetail.put("userEmail", user.getEmail());
                    itemDetail.put("userAvatar", user.getAvatar());
                } else {
                    itemDetail.put("userName", "匿名用户");
                    itemDetail.put("userPhone", null);
                    itemDetail.put("userEmail", null);
                    itemDetail.put("userAvatar", null);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败: userId={}, error={}", item.getSubmitterId(), e.getMessage());
                itemDetail.put("userName", "匿名用户");
                itemDetail.put("userPhone", null);
                itemDetail.put("userEmail", null);
                itemDetail.put("userAvatar", null);
            }
            
            // 联系方式字段
            itemDetail.put("contact", item.getContact());
            
            log.info("获取物品详情成功: id={}, itemName={}, userName={}", 
                    id, item.getItemName(), itemDetail.get("userName"));
            
            return Result.success("获取成功", itemDetail);
            
        } catch (Exception e) {
            log.error("获取物品详情失败: id={}", id, e);
            return Result.error("获取物品详情失败：" + e.getMessage());
        }
    }
    
    // 4. 更新 - 修改物品信息
    @PutMapping("/items/{id}")
    public Result<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        // 1. 验证权限 (是否为发布者)
        // 2. 调用Service更新
        // 3. 返回更新结果
    }
    
    // 5. 删除 - 删除物品
    @DeleteMapping("/items/{id}")
    public Result deleteItem(@PathVariable Long id) {
        // 1. 验证权限
        // 2. 调用Service删除(逻辑删除)
        // 3. 返回结果
    }
}
```

### Service层 (ItemServiceImpl.java)

```java
@Service
@Transactional
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    
    @Autowired
    private ItemMapper itemMapper;
    
    // 1. 分页查询 - 调用MyBatis Plus进行数据库查询
    @Override
    public Page<Item> getItemPage(int current, int pageSize, String type, ...) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getType, type)
               .eq(StringUtils.hasText(category), Item::getCategory, category)
               .eq(Item::getDeleted, 0)
               .orderByDesc(Item::getCreatedAt);
        
        // 2. 执行分页查询
        Page<Item> page = new Page<>(current, pageSize);
        return this.page(page, wrapper);
    }
    
    // 2. 创建 - 保存物品到数据库
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Item publishLostItem(Item item, Long submitterId) {
        if (item == null || submitterId == null) {
            return null;
        }
        
        // 设置发布信息 - 确保type为lost，但使用前端传递的值
        if (item.getType() == null || !item.getType().equals("lost")) {
            item.setType("lost");
        }
        item.setSubmitterId(submitterId);
        item.setStatus("pending");
        
        // 保存到数据库
        this.save(item);
        
        log.info("发布失物成功: itemId={}, submitterId={}", item.getId(), submitterId);
        return item;
    }
    
    // 3. 详情查询 - 根据ID查询物品和用户信息
    @Override
    public Item getItemDetail(Long id) {
        // 1. 查询物品基本信息
        Item item = this.getById(id);
        if (item == null || item.getDeleted() == 1) {
            return null;
        }
        
        // 2. 可以添加关联查询逻辑
        // 如需要查询评论、线索等关联数据
        
        return item;
    }
    
    // 4. 更新 - 修改物品信息
    @Override
    @Transactional
    public boolean updateItem(Item item, Long userId) {
        // 1. 验证权限
        Item existing = this.getById(item.getId());
        if (existing == null || !existing.getSubmitterId().equals(userId)) {
            return false;
        }
        
        // 2. 更新字段
        item.setUpdatedAt(LocalDateTime.now());
        
        // 3. 执行更新
        return this.updateById(item);
    }
    
    // 5. 删除 - 逻辑删除
    @Override
    @Transactional
    public boolean deleteItem(Long id, Long userId) {
        // 1. 验证权限
        Item item = this.getById(id);
        if (item == null || !item.getSubmitterId().equals(userId)) {
            return false;
        }
        
        // 2. 逻辑删除
        item.setDeleted(1);
        return this.updateById(item);
    }
}
```

### Mapper层 (ItemMapper.java)

```java
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    
    // 基础CRUD操作由MyBatis Plus提供:
    // - insert(Item item)           // 新增
    // - deleteById(Long id)         // 删除
    // - updateById(Item item)        // 更新
    // - selectById(Long id)         // 查询
    // - selectPage(Page, Wrapper)   // 分页查询
    
    // 复杂查询可以自定义SQL
    @Select("SELECT * FROM items WHERE type = #{type} AND deleted = 0")
    List<Item> findByType(@Param("type") String type);
}
```

### Entity层 (Item.java)

```java
@Data
@TableName("items")
public class Item implements Serializable {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;                          // 物品ID
    
    @TableField("item_name")
    private String itemName;                     // 物品名称
    
    @TableField("category")
    private String category;                     // 物品分类
    
    @TableField("description")
    private String description;                  // 物品描述
    
    @TableField("images")
    private String images;                       // 图片URLs(JSON)
    
    @TableField("location")
    private String location;                     // 丢失/发现地点
    
    @TableField("event_time")
    private LocalDate eventTime;                 // 事件时间
    
    @TableField("contact")
    private String contact;                      // 联系方式
    
    @TableField("type")
    private String type;                         // 类型: lost/found
    
    @TableField("status")
    private String status;                       // 状态
    
    @TableField("submitter_id")
    private Long submitterId;                   // 提交者ID
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;                // 创建时间
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;                // 更新时间
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;                      // 逻辑删除标记
}
```

## 前端CRUD操作

### 1. 查询操作 (列表页)

#### 页面结构 (pages/user/lost-found.vue)
```vue
<template>
  <view class="lost-found-container">
    <!-- 搜索和筛选 -->
    <view class="search-section">
      <input v-model="searchForm.keyword" placeholder="搜索物品..." />
      <picker :value="searchForm.category" @change="onCategoryChange">
        <view>选择分类</view>
      </picker>
      <button @click="loadItems">搜索</button>
    </view>
    
    <!-- 物品列表 -->
    <scroll-view scroll-y @scrolltolower="loadMore">
      <view v-for="item in itemsList" :key="item.id" @click="goToDetail(item.id)">
        <image :src="item.images[0]" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.itemName }}</text>
          <text class="item-category">{{ item.category }}</text>
          <text class="item-location">{{ item.location }}</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 发布按钮 -->
    <button class="fab-button" @click="goToPublish">发布</button>
  </view>
</template>
```

#### 数据发送流程
```javascript
// 1. 页面初始化
onLoad() {
  this.loadItems()  // 加载第一页数据
}

// 2. 查询数据
async loadItems(loadMore = false) {
  try {
    // 构建请求参数
    const params = {
      current: this.currentPage,
      pageSize: 10,
      keyword: this.searchForm.keyword,
      category: this.searchForm.category,
      type: this.currentType  // 'lost' 或 'found'
    }
    
    // 发送请求
    const result = await getLostItems(params)
    
    if (result.success) {
      if (loadMore) {
        // 加载更多：追加数据
        this.itemsList = [...this.itemsList, ...result.data.list]
      } else {
        // 刷新：替换数据
        this.itemsList = result.data.list
      }
      this.hasMore = result.data.list.length === 10
    }
  } catch (error) {
    console.error('加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

// 3. 下拉刷新
onPullDownRefresh() {
  this.currentPage = 1
  this.loadItems(false)
  uni.stopPullDownRefresh()
}

// 4. 上拉加载更多
onReachBottom() {
  if (this.hasMore) {
    this.currentPage++
    this.loadItems(true)
  }
}
```

### 2. 创建操作 (发布页面)

#### 页面结构 (pages/user/publish-lost.vue)
```vue
<template>
  <view class="publish-container">
    <form @submit.prevent="submitForm">
      <!-- 基本信息 -->
      <view class="form-section">
        <input v-model="form.itemName" placeholder="物品名称" required />
        <picker v-model="form.category" :range="categories">
          <view>选择分类</view>
        </picker>
        <textarea v-model="form.description" placeholder="详细描述" required />
      </view>
      
      <!-- 时间地点 -->
      <view class="form-section">
        <picker v-model="form.eventDate" mode="date">
          <view>丢失日期</view>
        </picker>
        <input v-model="form.location" placeholder="丢失地点" />
      </view>
      
      <!-- 联系方式 -->
      <view class="form-section">
        <input v-model="form.contact" placeholder="联系方式" />
      </view>
      
      <!-- 图片上传 -->
      <view class="form-section">
        <view class="image-upload">
          <view v-for="(image, index) in imageList" :key="index">
            <image :src="image" mode="aspectFill" />
            <button @click="removeImage(index)">删除</button>
          </view>
          <button @click="chooseImage">添加图片</button>
        </view>
      </view>
      
      <!-- 提交按钮 -->
      <button type="submit" :disabled="!isFormValid" class="submit-btn">
        {{ isSubmitting ? '发布中...' : '发布失物' }}
      </button>
    </form>
  </view>
</template>
```

#### 数据创建流程
```javascript
// 1. 表单数据
data() {
  return {
    form: {
      itemName: '',
      category: '',
      description: '',
      eventDate: '',
      location: '',
      contact: ''
    },
    imageList: [],
    isSubmitting: false
  }
}

// 2. 表单验证
computed: {
  isFormValid() {
    return this.form.itemName.trim() && 
           this.form.description.trim() && 
           this.form.location.trim()
  }
}

// 3. 图片上传
async chooseImage() {
  const res = await uni.chooseImage({
    count: 6 - this.imageList.length,
    sizeType: ['compressed']
  })
  
  // 上传图片到服务器
  for (const file of res.tempFilePaths) {
    const uploadResult = await uploadImage(file)
    this.imageList.push(uploadResult.url)
  }
}

// 4. 提交表单
async submitForm() {
  if (!this.isFormValid || this.isSubmitting) return
  
  this.isSubmitting = true
  
  try {
    // 构建提交数据
    const submitData = {
      itemName: this.form.itemName,
      category: this.form.category,
      description: this.form.description,
      lostTime: this.form.eventDate,
      lostLocation: this.form.location,
      contact: this.form.contact,
      images: JSON.stringify(this.imageList),
      type: 'lost'
    }
    
    // 发送创建请求
    const result = await publishLostItem(submitData)
    
    if (result.success) {
      uni.showToast({ title: '发布成功', icon: 'success' })
      
      // 跳转到详情页
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/user/item-detail?id=${result.data.id}`
        })
      }, 1500)
    }
  } catch (error) {
    console.error('发布失败:', error)
    uni.showToast({ title: '发布失败', icon: 'none' })
  } finally {
    this.isSubmitting = false
  }
}
```

### 3. 更新操作

#### 更新流程
```javascript
// 1. 编辑模式 - 加载现有数据
onLoad(options) {
  if (options.id && options.edit === 'true') {
    this.isEditMode = true
    this.loadItemData(options.id)
  }
}

async loadItemData(id) {
  const result = await getItemDetail(id)
  if (result.success) {
    // 填充表单
    this.form = {
      itemName: result.data.itemName,
      category: result.data.category,
      description: result.data.description,
      location: result.data.location,
      contact: result.data.contact
    }
    this.itemId = id
  }
}

// 2. 提交更新
async submitForm() {
  const updateData = { ...this.form }
  
  const result = await updateItem(this.itemId, updateData)
  
  if (result.success) {
    uni.showToast({ title: '更新成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
}
```

### 4. 删除操作

#### 删除确认流程
```javascript
// 删除按钮点击
async confirmDelete(itemId) {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除吗？',
    success: async (res) => {
      if (res.confirm) {
        await this.deleteItem(itemId)
      }
    }
  })
}

// 执行删除
async deleteItem(itemId) {
  try {
    const result = await deleteItem(itemId)
    
    if (result.success) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      
      // 从列表中移除
      this.itemsList = this.itemsList.filter(item => item.id !== itemId)
    }
  } catch (error) {
    console.error('删除失败:', error)
    uni.showToast({ title: '删除失败', icon: 'none' })
  }
}
```

## API接口规范

### 请求/响应格式

#### 统一响应格式
```json
{
  "success": true,                    // 操作是否成功
  "message": "操作成功",              // 提示信息
  "data": {                          // 业务数据
    // 具体数据内容
  },
  "timestamp": 1642857600000          // 时间戳
}
```

#### 错误响应
```json
{
  "success": false,
  "message": "错误信息",
  "data": null,
  "timestamp": 1642857600000
}
```

### 接口列表

#### 1. 查询接口
```http
GET /api/items/lost-items
Content-Type: application/json
Authorization: Bearer {token}

参数:
- current: 1                 // 页码
- pageSize: 10               // 每页数量  
- category: "电子产品"        // 分类(可选)
- keyword: "手机"           // 关键词(可选)
- location: "图书馆"          // 地点(可选)

响应:
{
  "success": true,
  "message": "获取成功",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 45,
        "itemName": "iPhone手机",
        "category": "电子产品", 
        "description": "黑色iPhone 13...",
        "images": ["url1", "url2"],
        "location": "图书馆",
        "eventTime": "2024-12-20",
        "type": "lost",
        "status": "pending",
        "userName": "张三",
        "userPhone": "138****5678",
        "userEmail": "zhang***@email.com",
        "created_at": "2024-12-20T10:30:00"
      }
    ]
  }
}
```

#### 2. 创建接口
```http
POST /api/items/lost-items
Content-Type: application/json
Authorization: Bearer {token}

请求体:
{
  "itemName": "华为手机",
  "category": "电子产品",
  "description": "白色华为P40，在食堂丢失",
  "lostTime": "2024-12-22",
  "lostLocation": "第二食堂",
  "contact": "13800138000",
  "images": "[\"url1\", \"url2\"]",
  "type": "lost"
}

响应:
{
  "success": true,
  "message": "发布成功",
  "data": {
    "id": 46,
    "itemName": "华为手机",
    "status": "pending",
    "created_at": "2024-12-22T15:30:00"
  }
}
```

#### 3. 查询详情接口
```http
GET /api/items/45/detail
Content-Type: application/json
Authorization: Bearer {token} (可选)

响应:
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 45,
    "itemName": "啊啊啊",
    "category": "钱包证件",
    "description": "啊啊啊",
    "images": ["http://.../image.jpg"],
    "location": "啊啊啊",
    "eventTime": "2025-12-22",
    "type": "lost",
    "status": "pending",
    "userName": "111",
    "userPhone": "13959537729",
    "userEmail": "111111111@qq.com",
    "submitterId": 26,
    "contact": "啊啊啊",
    "created_at": "2025-12-22T20:19:22"
  }
}
```

#### 4. 更新接口
```http
PUT /api/items/45
Content-Type: application/json
Authorization: Bearer {token}

请求体:
{
  "itemName": "修改后的名称",
  "category": "电子产品",
  "description": "修改后的描述",
  "location": "修改后的地点",
  "contact": "修改后的联系方式"
}

响应:
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": 45,
    "itemName": "修改后的名称",
    "updatedAt": "2024-12-22T16:00:00"
  }
}
```

#### 5. 删除接口
```http
DELETE /api/items/45
Content-Type: application/json
Authorization: Bearer {token}

响应:
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```

## 数据流向图

### 完整的CRUD数据流

```
1. 用户操作 → 前端页面
   ┌─────────────────────┐
   │  用户点击"发布失物" │
   └─────────┬───────────┘
             │
2. 前端验证 → 数据准备
   ┌─────────────────────┐
   │  表单验证       │
   │  数据转换        │
   │  图片上传        │
   └─────────┬───────────┘
             │
3. HTTP请求 → 后端Controller
   ┌─────────────────────┐
   │  POST /items/lost-items │
   │  认证验证        │
   │  数据绑定        │
   └─────────┬───────────┘
             │
4. Controller → Service层
   ┌─────────────────────┐
   │  业务逻辑处理      │
   │  数据验证        │
   │  权限检查        │
   └─────────┬───────────┘
             │
5. Service → Mapper层
   ┌─────────────────────┐
   │  SQL构建        │
   │  数据库操作      │
   │  事务管理        │
   └─────────┬───────────┘
             │
6. Mapper → 数据库
   ┌─────────────────────┐
   │  INSERT INTO items │
   │  事务提交        │
   │  返回自增ID      │
   └─────────┬───────────┘
             │
7. 数据库返回 ← Mapper ← Service ← Controller ← 前端
   ┌─────────────────────────────────────────────────────────────┐
   │  { "id": 45, "itemName": "...", "success": true }    │
   │  逐层返回并处理最终结果                           │
   └─────────────────────────────────────────────────────────────┘
```

### 错误处理流程

```
1. 数据库约束违反
   Database → Mapper → Service → Controller
   │              │           │
   │              │           ▼
   │              │    "物品名称不能为空"
   │              ▼
   │         Service抛出业务异常
   ▼
   Controller统一异常处理
   
2. 权限验证失败
   JWT验证 → Controller → 前端
      │           │
      │           ▼
      │         "无权限操作此物品"
      ▼
   前端提示错误并跳转登录页
   
3. 网络异常
   前端 → 后端
      │
      ▼
   "网络请求失败"
   前端显示错误提示并提供重试
```

## 最佳实践

### 1. 数据安全
- 使用JWT Token进行身份验证
- 敏感信息脱敏显示 (手机号、邮箱)
- 输入验证和SQL注入防护
- 权限校验 (只能操作自己的物品)

### 2. 性能优化
- 分页查询避免大量数据
- 图片压缩和CDN加速
- 数据库索引优化
- 缓存常用查询

### 3. 用户体验
- 加载状态提示
- 操作结果反馈
- 错误友好提示
- 离线数据处理

### 4. 代码规范
- 统一响应格式
- 统一异常处理
- 参数验证
- 日志记录

---

## 总结

这份文档详细描述了失物招领系统从用户操作到数据库存储的完整CRUD流程：

1. **前端**: 负责用户交互、数据展示和HTTP请求
2. **Controller**: 接收请求、参数验证、调用Service
3. **Service**: 业务逻辑处理、事务管理、调用Mapper  
4. **Mapper**: 数据库操作、SQL执行
5. **数据库**: 数据持久化、事务保证

每次CRUD操作都遵循这个流程，确保数据的一致性和系统的可靠性。