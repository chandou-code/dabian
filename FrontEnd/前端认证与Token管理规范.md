# 校园失物招领系统 - 前端认证与Token管理规范

## 1. 认证流程概述

### 1.1 完整认证流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 登录页面    │────▶│ 调用登录API │────▶│ 后端验证    │
└─────────────┘     └─────────────┘     └─────────────┘
                         ▲                      │
                         │                      ▼
                         │               ┌─────────────┐
                         │               │ 返回用户信息│
                         │               │ 和Token     │
                         │               └─────────────┘
                         │                      │
                         │                      ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 本地存储    │◀────│ Vuex状态管理 │◀────│ 保存用户信息│
└─────────────┘     └─────────────┘     └─────────────┘
                         │
                         ▼
                    ┌─────────────┐
                    │ 请求拦截器  │
                    │ 自动添加Token│
                    └─────────────┘
                         │
                         ▼
                    ┌─────────────┐
                    │ API请求     │
                    │ 携带Token   │
                    └─────────────┘
```

## 2. 登录实现

### 2.1 登录页面

#### 核心登录逻辑

```javascript
async handleLogin() {
  if (!this.validateForm()) {
    return
  }
  
  this.isLoading = true
  
  try {
    // 调用真实登录API
    const response = await apiLogin(this.loginForm)
    
    console.log('登录响应:', response)
    
    // 检查响应数据结构
    if (response.code === 200 && response.data) {
      const { user, token } = response.data
      
      if (!user || !user.role) {
        throw new Error('用户信息不完整，缺少role字段')
      }
      
      // 登录成功，保存用户信息和token
      this.login({
        user: user,
        token: token
      })
    } else {
      throw new Error(response.message || '登录失败')
    }
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      this.redirectToDashboard()
    }, 1500)
    
  } catch (error) {
    uni.showToast({
      title: error.message || '登录失败',
      icon: 'none'
    })
  } finally {
    this.isLoading = false
  }
}
```

### 2.2 登录API

```javascript
// auth.js - 认证相关API
import { post } from './request'

// 用户登录
export const login = (credentials) => {
  return post('/auth/login', credentials)
}

// 用户注册
export const register = (userInfo) => {
  return post('/auth/register', userInfo)
}

// 刷新Token
export const refreshToken = (refreshToken) => {
  return post('/auth/refresh-token', { refreshToken })
}
```

## 3. Token管理

### 3.1 Vuex状态管理

#### 状态定义

```javascript
const store = new Vuex.Store({
  state: {
    user: null,
    token: '',
    isLoggedIn: false,
    userRole: null // 'user', 'admin', 'reviewer'
  },
  // ...
})
```

#### Mutation操作

```javascript
mutations: {
  SET_USER(state, user) {
    console.log('SET_USER mutation被调用，用户信息:', user)
    state.user = user
    state.isLoggedIn = true
    state.userRole = user ? user.role : null
    console.log('设置用户角色:', state.userRole)
  },
  
  SET_TOKEN(state, token) {
    state.token = token
    uni.setStorageSync('token', token)
  },
  
  LOGOUT(state) {
    state.user = null
    state.token = ''
    state.isLoggedIn = false
    state.userRole = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
  },
  
  INIT_USER(state) {
    const token = uni.getStorageSync('token')
    const user = uni.getStorageSync('user')
    
    if (token && user) {
      state.token = token
      state.user = user
      state.isLoggedIn = true
      state.userRole = user.role
    }
  }
}
```

#### Action操作

```javascript
actions: {
  login({ commit }, { user, token }) {
    console.log('store login action被调用:', { user, token })
    commit('SET_USER', user)
    commit('SET_TOKEN', token)
    uni.setStorageSync('user', user)
    console.log('用户信息和token已保存到storage')
  },
  
  logout({ commit }) {
    commit('LOGOUT')
  },
  
  initUser({ commit }) {
    commit('INIT_USER')
  }
}
```

#### Getter计算属性

```javascript
getters: {
  isLoggedIn: state => state.isLoggedIn,
  userRole: state => state.userRole,
  user: state => state.user,
  token: state => state.token
}
```

### 3.2 Token持久化

- **存储方式**：使用 `uni.setStorageSync` 将token和用户信息保存到本地存储
- **恢复机制**：页面初始化时调用 `initUser` action从本地存储恢复数据
- **清除机制**：退出登录时调用 `LOGOUT` mutation清除所有状态和本地存储

## 4. 请求拦截与Token携带

### 4.1 请求拦截器

```javascript
// 封装请求函数
export const request = (options) => {
  // 从本地存储获取token
  const token = uni.getStorageSync('token')
  
  // 配置默认参数
  const config = {
    url: `${baseURL}${options.url}`,
    method: options.method || 'GET',
    header: {
      'Content-Type': 'application/json',
      ...options.header
    },
    data: options.data || {}
  }
  
  // 如果有token，添加到请求头
  if (token) {
    config.header.Authorization = `Bearer ${token}`
  }
  
  // ...
}
```

### 4.2 Token格式

使用 **Bearer Token** 格式，示例：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

## 5. 认证状态管理

### 5.1 初始化认证状态

在应用启动时初始化用户状态：

```javascript
// App.vue
onLaunch: function() {
  console.log('App Launch')
  // 初始化用户状态
  this.$store.dispatch('initUser')
}
```

### 5.2 检查登录状态

在需要登录的页面检查登录状态：

```javascript
// 登录页面onLoad钩子
onLoad() {
  // 检查是否已登录
  this.checkLoginStatus()
},

methods: {
  checkLoginStatus() {
    this.initUser()
    if (this.$store.getters.isLoggedIn) {
      this.redirectToDashboard()
    }
  },
  // ...
}
```

### 5.3 路由守卫

对于需要登录才能访问的页面，可以添加路由守卫：

```javascript
// 页面onLoad钩子
onLoad() {
  // 检查登录状态
  if (!this.$store.getters.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  
  // 检查用户角色
  if (this.$store.getters.userRole !== 'admin') {
    uni.navigateTo({ url: '/pages/user/dashboard' })
    return
  }
  
  // 页面正常加载
  this.loadData()
}
```

## 6. 错误处理

### 6.1 Token过期处理

在请求响应中处理401未授权错误：

```javascript
uni.request({
  ...config,
  success: (res) => {
    // ...
    } else if (res.statusCode === 401) {
      // 未授权，跳转到登录页
      console.error('未授权访问，状态码:', res.statusCode)
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      uni.navigateTo({
        url: '/pages/login/login'
      })
      reject(res)
    } else {
      // 其他HTTP错误
      // ...
    }
  },
  // ...
})
```

### 6.2 网络错误处理

```javascript
fail: (err) => {
  // 网络错误
  console.error('网络请求失败:', err)
  uni.showToast({
    title: '网络错误，请稍后重试',
    icon: 'none'
  })
  reject(err)
}
```

## 7. 退出登录

### 7.1 退出登录逻辑

```javascript
handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        this.$store.dispatch('logout')
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
```

### 7.2 清除所有状态

```javascript
LOGOUT(state) {
  state.user = null
  state.token = ''
  state.isLoggedIn = false
  state.userRole = null
  uni.removeStorageSync('token')
  uni.removeStorageSync('user')
}
```

## 8. 角色权限管理

### 8.1 角色定义

| 角色 | 标识 | 权限范围 |
|------|------|----------|
| 用户 | user | 个人中心、发布信息、搜索、查看信息 |
| 审核员 | reviewer | 审核信息、统计报表 |
| 管理员 | admin | 系统管理、用户管理、审核员管理 |

### 8.2 角色验证

在页面或组件中验证用户角色：

```javascript
// 根据角色显示不同内容
<view v-if="$store.getters.userRole === 'admin'" class="admin-section">
  <!-- 管理员专属内容 -->
</view>

// 根据角色跳转到不同页面
redirectToDashboard() {
  const role = this.$store.getters.userRole
  let route = '/pages/user/dashboard'
  
  if (role === 'admin') {
    route = '/pages/admin/dashboard'
  } else if (role === 'reviewer') {
    route = '/pages/reviewer/dashboard'
  }
  
  uni.reLaunch({ url: route })
}
```

## 9. 安全最佳实践

### 9.1 Token安全

1. **使用HTTPS**：生产环境必须使用HTTPS协议传输Token
2. **合理设置Token过期时间**：根据业务需求设置合适的过期时间
3. **避免Token泄露**：
   - 不要将Token存储在localStorage（易受XSS攻击）
   - 使用uni.setStorageSync（小程序安全存储）
   - 避免在URL中传递Token
4. **定期刷新Token**：实现Token刷新机制，避免用户频繁登录

### 9.2 权限验证

1. **前端验证**：用于提升用户体验，快速反馈
2. **后端验证**：作为最终验证，确保数据安全
3. **最小权限原则**：只授予用户必要的权限
4. **权限动态更新**：当用户权限变化时，及时更新前端状态

### 9.3 数据安全

1. **敏感数据加密**：对敏感数据进行加密传输
2. **避免明文存储密码**：前端永远不要存储用户密码
3. **输入验证**：对所有用户输入进行验证，防止注入攻击
4. **请求频率限制**：防止暴力破解和DOS攻击

## 10. 示例代码

### 10.1 完整登录组件示例

```javascript
<template>
  <view class="login-container">
    <view class="login-card">
      <!-- 登录表单 -->
      <view class="login-form">
        <view class="form-item">
          <input v-model="loginForm.username" placeholder="用户名" />
        </view>
        <view class="form-item">
          <input v-model="loginForm.password" type="password" placeholder="密码" />
        </view>
        <button @click="handleLogin">登录</button>
      </view>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'
import { login as apiLogin } from '../../api'

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      }
    }
  },
  
  methods: {
    ...mapActions(['login', 'initUser']),
    
    async handleLogin() {
      try {
        const response = await apiLogin(this.loginForm)
        
        if (response.code === 200 && response.data) {
          const { user, token } = response.data
          this.login({ user, token })
          uni.showToast({ title: '登录成功' })
          uni.reLaunch({ url: '/pages/user/dashboard' })
        }
      } catch (error) {
        uni.showToast({ title: '登录失败', icon: 'none' })
      }
    }
  }
}
</script>
```

### 10.2 受保护页面示例

```javascript
<template>
  <view class="protected-page">
    <text v-if="$store.getters.isLoggedIn">欢迎，{{ $store.getters.user.username }}！</text>
    <text v-else>请先登录</text>
    
    <button @click="handleLogout">退出登录</button>
  </view>
</template>

<script>
export default {
  onLoad() {
    // 检查登录状态
    if (!this.$store.getters.isLoggedIn) {
      uni.navigateTo({ url: '/pages/login/login' })
      return
    }
    
    // 检查用户角色
    if (this.$store.getters.userRole !== 'admin') {
      uni.navigateTo({ url: '/pages/user/dashboard' })
      return
    }
  },
  
  methods: {
    handleLogout() {
      this.$store.dispatch('logout')
      uni.navigateTo({ url: '/pages/login/login' })
    }
  }
}
</script>
```

## 11. 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2026-01-03 | 初始版本，定义前端认证与Token管理规范 |

---

**说明：** 本规范用于指导校园失物招领系统前端认证功能开发，确保所有认证相关代码遵循统一的安全标准和最佳实践。开发过程中应严格遵循本规范，如有特殊需求需修改规范，需经过开发团队和安全团队审核。