// 用户统计相关API
import { get } from './request'

// 获取用户统计信息
export const getUserStats = () => {
  return get('/items/stats/user')
}

// 获取用户仪表板数据（新接口）
export const getDashboardData = () => {
  return get('/statistics/dashboard/v2')
}

// 获取用户仪表板数据（兼容接口）
export const getDashboardDataCompat = () => {
  return get('/statistics/dashboard')
}

// 获取用户最近活动（分页）
export const getDashboardActivities = (page = 1, size = 10) => {
  return get('/statistics/dashboard/activities', { page, size })
}

// 获取用户推荐匹配（分页）
export const getDashboardMatches = (page = 1, size = 10) => {
  return get('/statistics/dashboard/matches', { page, size })
}

// 获取智能推荐匹配
export const getAiMatches = () => {
  return get('/statistics/dashboard/matches/ai')
}

// 获取首页统计数据（公开接口，无需登录）
export const getHomeStats = () => {
  return get('/home')
}

// 获取首页最新物品列表（公开接口，无需登录）
export const getHomeRecentItems = (limit = 10) => {
  return get('/home/recent', { limit })
}