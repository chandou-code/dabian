// 基础请求工具
const baseURL = 'http://localhost:18083/api'

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
    try {
      const queryString = Object.keys(options.params)
        .map(key => {
          const value = options.params[key]
          // 如果值为null或undefined，跳过该参数
          if (value === null || value === undefined) {
            return ''
          }
          return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`
        })
        .filter(item => item !== '') // 过滤掉空字符串
        .join('&')
      
      if (queryString) {
        config.url += (config.url.includes('?') ? '&' : '?') + queryString
      }
    } catch (error) {
      console.error('处理GET请求参数失败:', error)
    }
  }
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...config,
      success: (res) => {
        if (res.statusCode === 200) {
          const response = res.data
          
          if (response.code === 200 || response.code === 0) {
            resolve(response)
          } else {
            uni.showToast({
              title: response.msg || response.message || '请求失败',
              icon: 'none'
            })
            reject(response)
          }
        } else if (res.statusCode === 401) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          uni.navigateTo({
            url: '/pages/login/login'
          })
          reject(res)
        } else {
          uni.showToast({
            title: `请求错误: ${res.statusCode}`,
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
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

// 封装文件上传
export const upload = (url, options = {}) => {
  const token = uni.getStorageSync('token')
  
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${baseURL}${url}`,
      filePath: options.filePath || options.files[0].uri,
      name: options.name || options.files[0].name,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      formData: options.formData || {},
      success: (res) => {
        if (res.statusCode === 200) {
          const response = JSON.parse(res.data)
          if (response.code === 200 || response.code === 0) {
            resolve(response)
          } else {
            uni.showToast({
              title: response.msg || response.message || '上传失败',
              icon: 'none'
            })
            reject(response)
          }
        } else {
          uni.showToast({
            title: `上传错误: ${res.statusCode}`,
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 将upload方法添加到request对象上
request.upload = upload
