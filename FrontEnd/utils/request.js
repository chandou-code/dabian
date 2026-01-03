// 封装uni.request为类似axios的API
const BASE_URL = 'http://localhost:18080/api'

function request(options) {
  return new Promise((resolve, reject) => {
    // 添加token等请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    // 获取存储的token
    try {
      const token = uni.getStorageSync('token')
      if (token) {
        header['Authorization'] = `Bearer ${token}`
      }
    } catch (e) {
      console.log('获取token失败:', e)
    }
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: header,
      params: options.params,
      success: (res) => {
        console.log('请求成功:', res)
        if (res.statusCode === 200) {
          // 检查后端返回的数据格式
          if (res.data.success !== undefined) {
            // 后端已返回标准格式，直接返回
            resolve(res.data)
          } else {
            // 后端返回原始数据，包装成标准格式
            resolve({
              success: true,
              data: res.data,
              message: '请求成功'
            })
          }
        } else if (res.statusCode === 401) {
          // token过期，跳转登录
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          uni.navigateTo({
            url: '/pages/login/login'
          })
          reject(new Error('未授权'))
        } else {
          resolve({
            success: false,
            message: res.data?.message || `请求失败(${res.statusCode})`,
            data: null
          })
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        reject(err)
      }
    })
  })
}

export default request