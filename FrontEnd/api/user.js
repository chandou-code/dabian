// 用户管理相关API
import { get, put, post, del } from './request'

// 获取用户列表
export const getUserList = (params) => {
  return get('/admin/users', params)
}

// 更新用户状态
export const updateUserStatus = (userId, status) => {
  return put(`/admin/users/${userId}/status`, { status })
}

// 重置用户密码
export const resetUserPassword = (userId) => {
  return post(`/admin/users/${userId}/reset-password`)
}

// 批量操作用户
export const batchOperateUsers = (userIds, action) => {
  return post('/admin/users/batch', { userIds, action })
}

// 批量更新用户状态
export const batchUpdateUserStatus = (userIds, status) => {
  return put('/admin/users/batch-status', { userIds, status })
}

// 批量删除用户
export const batchDeleteUsers = (userIds) => {
  return del('/admin/users/batch-delete', { userIds })
}

// 更新用户信息
export const updateUserInfo = (userId, userInfo) => {
  return put(`/admin/users/${userId}`, userInfo)
}

// 导出用户数据
export const exportUsers = (params) => {
  return get('/admin/users/export', params)
}
