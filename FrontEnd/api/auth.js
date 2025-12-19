// 认证相关API
import { post } from './request'

// 用户注册
export const register = (userInfo) => {
  return post('/auth/register', userInfo)
}

// 用户登录
export const login = (credentials) => {
  return post('/auth/login', credentials)
}

// 用户登出
export const logout = () => {
  return post('/auth/logout')
}