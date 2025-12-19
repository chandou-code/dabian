// 基础请求工具
const baseURL = 'http://localhost:18080/api'

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
  
  // 处理GET请求的params参数
  if (options.method?.toUpperCase() === 'GET' && options.params) {
    // 将params转换为查询字符串
    const queryString = Object.keys(options.params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.params[key])}`)
      .join('&')
    
    // 添加查询字符串到URL
    if (queryString) {
      config.url += (config.url.includes('?') ? '&' : '?') + queryString
    }
  }
  
  // 返回Promise
  return new Promise((resolve, reject) => {
    console.log('===== 发送网络请求 =====')
    console.log('请求配置:', config)
    
    uni.request({
      ...config,
      success: (res) => {
        console.log('===== 请求响应 =====')
        console.log('响应状态码:', res.statusCode)
        console.log('响应数据:', res.data)
        console.log('响应数据类型:', typeof res.data)
        console.log('响应数据结构:', Object.keys(res.data))
        
        // 处理响应
        if (res.statusCode === 200) {
          const response = res.data
          
          // 检查业务状态码
          if (response.code === 200) {
            // 成功
            resolve(response.data)
          } else {
            // 业务错误
            console.error('业务错误:', response.message)
            uni.showToast({
              title: response.message || '请求失败',
              icon: 'none'
            })
            reject(response)
          }
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
          console.error('HTTP错误，状态码:', res.statusCode)
          uni.showToast({
            title: `请求错误: ${res.statusCode}`,
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        // 网络错误
        console.error('网络请求失败:', err)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 封装GET请求
export const get = (url, params, options = {}) => {
  return request({
    url,
    method: 'GET',
    params,
    ...options
  })
}

// 封装POST请求
export const post = (url, data, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

// 封装PUT请求
export const put = (url, data, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

// 封装DELETE请求
export const del = (url, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    ...options
  })
}