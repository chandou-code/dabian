// 系统相关API
import { get } from './request'

// 系统健康检查
export const healthCheck = () => {
  return get('/test/health')
}

// 获取管理员仪表板数据
export const getAdminDashboard = () => {
  return get('/admin/dashboard')
}

// 获取管理员仪表板最新动态
export const getAdminDashboardActivities = () => {
  return get('/admin/dashboard/activities')
}

// 获取失物招领趋势数据
export const getAdminTrendData = () => {
  return get('/admin/statistics/trend')
}

// 获取高频丢失物品数据
export const getAdminCategoryData = () => {
  return get('/admin/statistics/categories')
}

// 获取审核员列表
export const getReviewers = () => {
  return get('/admin/reviewers')
}

// 获取管理员统计数据
export const getAdminStats = (params) => {
  return get('/admin/stats', params)
}