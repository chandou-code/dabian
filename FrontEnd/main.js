import { createApp } from 'vue'
import App from './App'
import store from './store'

App.mpType = 'app'

// 检测URL中的token并自动登录
function checkUrlToken() {
  console.log('=== 开始检查URL中的token ===')
  console.log('当前URL:', window.location.href)
  
  // 同时检查search和hash中的token参数
  let token = null
  
  // 检查search参数
  console.log('检查search参数...')
  const searchParams = new URLSearchParams(window.location.search)
  token = searchParams.get('token')
  console.log('从search中获取的token:', token)
  
  // 如果search中没有，检查hash中的参数
  if (!token && window.location.hash.includes('?')) {
    console.log('检查hash参数...')
    // 处理 #/?token=xxx 格式的hash
    const hashPart = window.location.hash.split('?')[1]
    if (hashPart) {
      const hashParams = new URLSearchParams(hashPart)
      token = hashParams.get('token')
      console.log('从hash中获取的token:', token)
    }
  }
  
  // 额外处理 #/pages/login/login?token=xxx 格式的hash
  if (!token && window.location.hash.includes('token=')) {
    console.log('使用正则表达式检查hash中的token...')
    // 从完整hash中提取token
    const hash = window.location.hash
    const tokenMatch = hash.match(/token=([^&]+)/)
    if (tokenMatch && tokenMatch[1]) {
      token = decodeURIComponent(tokenMatch[1])
      console.log('使用正则表达式从hash中获取的token:', token)
    }
  }
  
  if (token) {
    console.log('=== 找到token，开始验证 ===')
    console.log('token长度:', token.length)
    
    // 从token中获取用户信息
    uni.request({
      url: 'http://localhost:18080/api/users/profile',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        console.log('=== Token验证响应 ===')
        console.log('响应状态:', res.statusCode)
        console.log('响应数据:', res.data)
        
        if (res.data && res.data.code === 200 && res.data.data) {
          const user = res.data.data
          console.log('=== Token验证成功，获取用户信息 ===')
          console.log('用户信息:', user)
          
          // 自动登录
          console.log('调用store.login action...')
          store.dispatch('login', { user, token })
          
          // 保存到本地存储
          console.log('保存token到本地存储...')
          uni.setStorageSync('token', token)
          console.log('保存用户信息到本地存储...')
          uni.setStorageSync('user', user)
          
          // 清除URL中的token参数，提高安全性
          console.log('清除URL中的token参数...')
          if (window.location.search.includes('token=')) {
            const url = new URL(window.location.href)
            url.searchParams.delete('token')
            window.history.replaceState({}, '', url.toString())
            console.log('清除search中的token参数')
          } else if (window.location.hash.includes('token=')) {
            const hash = window.location.hash.split('?')[0]
            window.history.replaceState({}, '', hash)
            console.log('清除hash中的token参数')
          }
          
          // 2秒后根据用户角色跳转到相应页面
          console.log('=== 2秒后跳转到相应页面 ===')
          console.log('用户角色:', user.role)
          setTimeout(() => {
            let redirectUrl = ''
            if (user.role === 'admin') {
              // 如果是管理员，跳转到管理员仪表板
              redirectUrl = 'http://localhost:5174/#/pages/admin/dashboard'
              console.log('跳转到管理员仪表板:', redirectUrl)
            } else if (user.role === 'reviewer') {
              // 如果是审核员，跳转到审核员仪表板
              redirectUrl = 'http://localhost:5174/#/pages/reviewer/dashboard'
              console.log('跳转到审核员仪表板:', redirectUrl)
            } else {
              // 普通用户，跳转到用户仪表板
              redirectUrl = 'http://localhost:5174/#/pages/user/dashboard'
              console.log('跳转到用户仪表板:', redirectUrl)
            }
            window.location.href = redirectUrl
          }, 2000)
        } else {
          console.log('=== Token验证失败，响应数据不符合要求 ===')
        }
      },
      fail: (err) => {
        console.log('=== Token验证请求失败 ===')
        console.error('Token验证失败:', err)
      }
    })
  } else {
    console.log('=== 未找到token ===')
  }
  console.log('=== 检查URL中的token结束 ===')
}

// 添加全局数据对象
const app = createApp({
  ...App,
  globalData: {
    selectedLocation: null
  }
})

app.use(store)
app.mount('#app')

// 立即检查URL Token，无论应用是否首次启动
checkUrlToken()